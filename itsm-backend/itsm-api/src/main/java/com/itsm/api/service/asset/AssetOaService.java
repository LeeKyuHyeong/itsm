package com.itsm.api.service.asset;

import com.itsm.api.dto.asset.*;
import com.itsm.core.domain.asset.AssetOa;
import com.itsm.core.domain.asset.AssetOaHistory;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.asset.AssetOaHistoryRepository;
import com.itsm.core.repository.asset.AssetOaRepository;
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
public class AssetOaService {

    private final AssetOaRepository assetOaRepository;
    private final AssetOaHistoryRepository assetOaHistoryRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AssetOaResponse> search(String keyword, Long companyId, String status,
                                         String assetTypeCd, Pageable pageable) {
        return assetOaRepository.search(keyword, companyId, status, assetTypeCd, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AssetOaResponse getDetail(Long assetOaId) {
        AssetOa asset = findById(assetOaId);
        return toResponse(asset);
    }

    public AssetOaResponse create(AssetOaCreateRequest req, Long currentUserId) {
        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "고객사를 찾을 수 없습니다."));

        User manager = null;
        if (req.getManagerId() != null) {
            manager = userRepository.findById(req.getManagerId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "담당자를 찾을 수 없습니다."));
        }

        AssetOa asset = AssetOa.builder()
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

        AssetOa saved = assetOaRepository.save(asset);
        return toResponse(saved);
    }

    public AssetOaResponse update(Long assetOaId, AssetOaUpdateRequest req, Long currentUserId) {
        AssetOa asset = findById(assetOaId);

        User manager = null;
        if (req.getManagerId() != null) {
            manager = userRepository.findById(req.getManagerId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "담당자를 찾을 수 없습니다."));
        }

        List<AssetOaHistory> histories = buildHistories(asset, req, currentUserId);
        if (!histories.isEmpty()) {
            assetOaHistoryRepository.saveAll(histories);
        }

        asset.update(req.getAssetNm(), req.getAssetTypeCd(), req.getManufacturer(),
                req.getModelNm(), req.getSerialNo(), req.getIpAddress(), req.getMacAddress(),
                req.getLocation(), req.getIntroducedAt(), req.getWarrantyEndAt(),
                manager, req.getDescription());
        asset.setUpdatedBy(currentUserId);

        return toResponse(asset);
    }

    public void changeStatus(Long assetOaId, String status, Long currentUserId) {
        AssetOa asset = findById(assetOaId);
        String beforeStatus = asset.getStatus();
        asset.changeStatus(status);

        AssetOaHistory history = AssetOaHistory.builder()
                .assetOaId(assetOaId)
                .changedField("status")
                .beforeValue(beforeStatus)
                .afterValue(status)
                .createdBy(currentUserId)
                .build();
        assetOaHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<AssetHistoryResponse> getHistory(Long assetOaId) {
        return assetOaHistoryRepository.findByAssetOaIdOrderByCreatedAtDesc(assetOaId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private AssetOa findById(Long assetOaId) {
        return assetOaRepository.findById(assetOaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "OA 자산을 찾을 수 없습니다."));
    }

    private List<AssetOaHistory> buildHistories(AssetOa asset, AssetOaUpdateRequest req, Long userId) {
        List<AssetOaHistory> histories = new ArrayList<>();
        Long id = asset.getAssetOaId();

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

    private void addHistoryIfChanged(List<AssetOaHistory> histories, Long assetOaId,
                                      String field, String before, String after, Long userId) {
        if (!Objects.equals(before, after)) {
            histories.add(AssetOaHistory.builder()
                    .assetOaId(assetOaId)
                    .changedField(field)
                    .beforeValue(before)
                    .afterValue(after)
                    .createdBy(userId)
                    .build());
        }
    }

    private AssetOaResponse toResponse(AssetOa asset) {
        return AssetOaResponse.builder()
                .assetOaId(asset.getAssetOaId())
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

    private AssetHistoryResponse toHistoryResponse(AssetOaHistory history) {
        return AssetHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .changedField(history.getChangedField())
                .beforeValue(history.getBeforeValue())
                .afterValue(history.getAfterValue())
                .createdBy(history.getCreatedBy())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
