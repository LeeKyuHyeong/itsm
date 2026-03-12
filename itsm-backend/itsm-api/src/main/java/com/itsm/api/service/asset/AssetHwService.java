package com.itsm.api.service.asset;

import com.itsm.api.dto.asset.*;
import com.itsm.core.domain.asset.*;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.asset.AssetHwHistoryRepository;
import com.itsm.core.repository.asset.AssetHwRepository;
import com.itsm.core.repository.asset.AssetRelationRepository;
import com.itsm.core.repository.asset.AssetSwRepository;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetHwService {

    private final AssetHwRepository assetHwRepository;
    private final AssetHwHistoryRepository assetHwHistoryRepository;
    private final AssetRelationRepository assetRelationRepository;
    private final AssetSwRepository assetSwRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AssetHwResponse> search(String keyword, Long companyId, String status,
                                         String assetTypeCd, Pageable pageable) {
        return assetHwRepository.search(keyword, companyId, status, assetTypeCd, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AssetHwResponse getDetail(Long assetHwId) {
        AssetHw asset = findById(assetHwId);
        return toResponse(asset);
    }

    public AssetHwResponse create(AssetHwCreateRequest req, Long currentUserId) {
        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "고객사를 찾을 수 없습니다."));

        User manager = null;
        if (req.getManagerId() != null) {
            manager = userRepository.findById(req.getManagerId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "담당자를 찾을 수 없습니다."));
        }

        AssetHw asset = AssetHw.builder()
                .assetNm(req.getAssetNm())
                .assetTypeCd(req.getAssetTypeCd())
                .manufacturer(req.getManufacturer())
                .modelNm(req.getModelNm())
                .serialNo(req.getSerialNo())
                .ipAddress(req.getIpAddress())
                .macAddress(req.getMacAddress())
                .location(req.getLocation())
                .introducedAt(req.getIntroducedAt())
                .warrantyEndAt(req.getWarrantyEndAt())
                .company(company)
                .manager(manager)
                .description(req.getDescription())
                .build();
        asset.setCreatedBy(currentUserId);

        AssetHw saved = assetHwRepository.save(asset);
        return toResponse(saved);
    }

    public AssetHwResponse update(Long assetHwId, AssetHwUpdateRequest req, Long currentUserId) {
        AssetHw asset = findById(assetHwId);

        User manager = null;
        if (req.getManagerId() != null) {
            manager = userRepository.findById(req.getManagerId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "담당자를 찾을 수 없습니다."));
        }

        List<AssetHwHistory> histories = buildHwHistories(asset, req, currentUserId);
        if (!histories.isEmpty()) {
            assetHwHistoryRepository.saveAll(histories);
        }

        asset.update(req.getAssetNm(), req.getAssetTypeCd(), req.getManufacturer(),
                req.getModelNm(), req.getSerialNo(), req.getIpAddress(), req.getMacAddress(),
                req.getLocation(), req.getIntroducedAt(), req.getWarrantyEndAt(),
                manager, req.getDescription());
        asset.setUpdatedBy(currentUserId);

        return toResponse(asset);
    }

    public void changeStatus(Long assetHwId, String status, Long currentUserId) {
        AssetHw asset = findById(assetHwId);
        String beforeStatus = asset.getStatus();
        asset.changeStatus(status);

        AssetHwHistory history = AssetHwHistory.builder()
                .assetHwId(assetHwId)
                .changedField("status")
                .beforeValue(beforeStatus)
                .afterValue(status)
                .createdBy(currentUserId)
                .build();
        assetHwHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<AssetHistoryResponse> getHistory(Long assetHwId) {
        return assetHwHistoryRepository.findByAssetHwIdOrderByCreatedAtDesc(assetHwId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    public AssetRelationResponse addRelation(AssetRelationRequest req, Long currentUserId) {
        AssetHw hw = findById(req.getAssetHwId());
        AssetSw sw = assetSwRepository.findById(req.getAssetSwId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "SW 자산을 찾을 수 없습니다."));

        AssetRelationId id = new AssetRelationId(req.getAssetHwId(), req.getAssetSwId());
        assetRelationRepository.findById(id).ifPresent(existing -> {
            if (existing.getRemovedAt() == null) {
                throw new BusinessException(ErrorCode.DUPLICATE_VALUE, "이미 등록된 연관관계입니다.");
            }
        });

        AssetRelation relation = AssetRelation.builder()
                .assetHwId(req.getAssetHwId())
                .assetSwId(req.getAssetSwId())
                .installedAt(req.getInstalledAt())
                .createdBy(currentUserId)
                .build();

        AssetRelation saved = assetRelationRepository.save(relation);
        return toRelationResponse(saved, hw.getAssetNm(), sw.getSwNm());
    }

    public void removeRelation(Long assetHwId, Long assetSwId) {
        AssetRelationId id = new AssetRelationId(assetHwId, assetSwId);
        AssetRelation relation = assetRelationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "연관관계를 찾을 수 없습니다."));
        relation.remove();
    }

    @Transactional(readOnly = true)
    public List<AssetRelationResponse> getRelations(Long assetHwId) {
        return assetRelationRepository.findByAssetHwIdAndRemovedAtIsNull(assetHwId)
                .stream()
                .map(r -> {
                    String hwNm = r.getAssetHw() != null ? r.getAssetHw().getAssetNm() : null;
                    String swNm = r.getAssetSw() != null ? r.getAssetSw().getSwNm() : null;
                    return toRelationResponse(r, hwNm, swNm);
                })
                .toList();
    }

    private AssetHw findById(Long assetHwId) {
        return assetHwRepository.findById(assetHwId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "HW 자산을 찾을 수 없습니다."));
    }

    private List<AssetHwHistory> buildHwHistories(AssetHw asset, AssetHwUpdateRequest req, Long userId) {
        List<AssetHwHistory> histories = new ArrayList<>();
        Long id = asset.getAssetHwId();

        addHistoryIfChanged(histories, id, "assetNm", asset.getAssetNm(), req.getAssetNm(), userId);
        addHistoryIfChanged(histories, id, "assetTypeCd", asset.getAssetTypeCd(), req.getAssetTypeCd(), userId);
        addHistoryIfChanged(histories, id, "manufacturer", asset.getManufacturer(), req.getManufacturer(), userId);
        addHistoryIfChanged(histories, id, "modelNm", asset.getModelNm(), req.getModelNm(), userId);
        addHistoryIfChanged(histories, id, "serialNo", asset.getSerialNo(), req.getSerialNo(), userId);
        addHistoryIfChanged(histories, id, "ipAddress", asset.getIpAddress(), req.getIpAddress(), userId);
        addHistoryIfChanged(histories, id, "macAddress", asset.getMacAddress(), req.getMacAddress(), userId);
        addHistoryIfChanged(histories, id, "location", asset.getLocation(), req.getLocation(), userId);
        addHistoryIfChanged(histories, id, "introducedAt",
                asset.getIntroducedAt() != null ? asset.getIntroducedAt().toString() : null,
                req.getIntroducedAt() != null ? req.getIntroducedAt().toString() : null, userId);
        addHistoryIfChanged(histories, id, "warrantyEndAt",
                asset.getWarrantyEndAt() != null ? asset.getWarrantyEndAt().toString() : null,
                req.getWarrantyEndAt() != null ? req.getWarrantyEndAt().toString() : null, userId);
        addHistoryIfChanged(histories, id, "description", asset.getDescription(), req.getDescription(), userId);

        return histories;
    }

    private void addHistoryIfChanged(List<AssetHwHistory> histories, Long assetHwId,
                                      String field, String before, String after, Long userId) {
        if (!Objects.equals(before, after)) {
            histories.add(AssetHwHistory.builder()
                    .assetHwId(assetHwId)
                    .changedField(field)
                    .beforeValue(before)
                    .afterValue(after)
                    .createdBy(userId)
                    .build());
        }
    }

    private AssetHwResponse toResponse(AssetHw asset) {
        return AssetHwResponse.builder()
                .assetHwId(asset.getAssetHwId())
                .assetNm(asset.getAssetNm())
                .assetTypeCd(asset.getAssetTypeCd())
                .manufacturer(asset.getManufacturer())
                .modelNm(asset.getModelNm())
                .serialNo(asset.getSerialNo())
                .ipAddress(asset.getIpAddress())
                .macAddress(asset.getMacAddress())
                .location(asset.getLocation())
                .introducedAt(asset.getIntroducedAt())
                .warrantyEndAt(asset.getWarrantyEndAt())
                .companyId(asset.getCompany() != null ? asset.getCompany().getCompanyId() : null)
                .companyNm(asset.getCompany() != null ? asset.getCompany().getCompanyNm() : null)
                .managerId(asset.getManager() != null ? asset.getManager().getUserId() : null)
                .managerNm(asset.getManager() != null ? asset.getManager().getUserNm() : null)
                .status(asset.getStatus())
                .description(asset.getDescription())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }

    private AssetHistoryResponse toHistoryResponse(AssetHwHistory history) {
        return AssetHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .changedField(history.getChangedField())
                .beforeValue(history.getBeforeValue())
                .afterValue(history.getAfterValue())
                .createdBy(history.getCreatedBy())
                .createdAt(history.getCreatedAt())
                .build();
    }

    private AssetRelationResponse toRelationResponse(AssetRelation relation, String hwNm, String swNm) {
        return AssetRelationResponse.builder()
                .assetHwId(relation.getAssetHwId())
                .assetHwNm(hwNm)
                .assetSwId(relation.getAssetSwId())
                .assetSwNm(swNm)
                .installedAt(relation.getInstalledAt())
                .removedAt(relation.getRemovedAt())
                .createdAt(relation.getCreatedAt())
                .build();
    }
}
