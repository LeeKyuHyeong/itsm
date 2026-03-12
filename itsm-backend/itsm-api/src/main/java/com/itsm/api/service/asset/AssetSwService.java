package com.itsm.api.service.asset;

import com.itsm.api.dto.asset.*;
import com.itsm.core.domain.asset.AssetSw;
import com.itsm.core.domain.asset.AssetSwHistory;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.asset.AssetSwHistoryRepository;
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
public class AssetSwService {

    private final AssetSwRepository assetSwRepository;
    private final AssetSwHistoryRepository assetSwHistoryRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AssetSwResponse> search(String keyword, Long companyId, String status,
                                         String swTypeCd, Pageable pageable) {
        return assetSwRepository.search(keyword, companyId, status, swTypeCd, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AssetSwResponse getDetail(Long assetSwId) {
        AssetSw sw = findById(assetSwId);
        return toResponse(sw);
    }

    public AssetSwResponse create(AssetSwCreateRequest req, Long currentUserId) {
        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "고객사를 찾을 수 없습니다."));

        User manager = null;
        if (req.getManagerId() != null) {
            manager = userRepository.findById(req.getManagerId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "담당자를 찾을 수 없습니다."));
        }

        AssetSw sw = AssetSw.builder()
                .swNm(req.getSwNm())
                .swTypeCd(req.getSwTypeCd())
                .version(req.getVersion())
                .licenseKey(req.getLicenseKey())
                .licenseCnt(req.getLicenseCnt())
                .installedAt(req.getInstalledAt())
                .expiredAt(req.getExpiredAt())
                .company(company)
                .manager(manager)
                .description(req.getDescription())
                .build();
        sw.setCreatedBy(currentUserId);

        AssetSw saved = assetSwRepository.save(sw);
        return toResponse(saved);
    }

    public AssetSwResponse update(Long assetSwId, AssetSwUpdateRequest req, Long currentUserId) {
        AssetSw sw = findById(assetSwId);

        User manager = null;
        if (req.getManagerId() != null) {
            manager = userRepository.findById(req.getManagerId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "담당자를 찾을 수 없습니다."));
        }

        List<AssetSwHistory> histories = buildSwHistories(sw, req, currentUserId);
        if (!histories.isEmpty()) {
            assetSwHistoryRepository.saveAll(histories);
        }

        sw.update(req.getSwNm(), req.getSwTypeCd(), req.getVersion(), req.getLicenseKey(),
                req.getLicenseCnt(), req.getInstalledAt(), req.getExpiredAt(),
                manager, req.getDescription());
        sw.setUpdatedBy(currentUserId);

        return toResponse(sw);
    }

    public void changeStatus(Long assetSwId, String status, Long currentUserId) {
        AssetSw sw = findById(assetSwId);
        String beforeStatus = sw.getStatus();
        sw.changeStatus(status);

        AssetSwHistory history = AssetSwHistory.builder()
                .assetSwId(assetSwId)
                .changedField("status")
                .beforeValue(beforeStatus)
                .afterValue(status)
                .createdBy(currentUserId)
                .build();
        assetSwHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<AssetHistoryResponse> getHistory(Long assetSwId) {
        return assetSwHistoryRepository.findByAssetSwIdOrderByCreatedAtDesc(assetSwId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private AssetSw findById(Long assetSwId) {
        return assetSwRepository.findById(assetSwId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "SW 자산을 찾을 수 없습니다."));
    }

    private List<AssetSwHistory> buildSwHistories(AssetSw sw, AssetSwUpdateRequest req, Long userId) {
        List<AssetSwHistory> histories = new ArrayList<>();
        Long id = sw.getAssetSwId();

        addHistoryIfChanged(histories, id, "swNm", sw.getSwNm(), req.getSwNm(), userId);
        addHistoryIfChanged(histories, id, "swTypeCd", sw.getSwTypeCd(), req.getSwTypeCd(), userId);
        addHistoryIfChanged(histories, id, "version", sw.getVersion(), req.getVersion(), userId);
        addHistoryIfChanged(histories, id, "licenseKey", sw.getLicenseKey(), req.getLicenseKey(), userId);
        addHistoryIfChanged(histories, id, "licenseCnt",
                sw.getLicenseCnt() != null ? sw.getLicenseCnt().toString() : null,
                req.getLicenseCnt() != null ? req.getLicenseCnt().toString() : null, userId);
        addHistoryIfChanged(histories, id, "installedAt",
                sw.getInstalledAt() != null ? sw.getInstalledAt().toString() : null,
                req.getInstalledAt() != null ? req.getInstalledAt().toString() : null, userId);
        addHistoryIfChanged(histories, id, "expiredAt",
                sw.getExpiredAt() != null ? sw.getExpiredAt().toString() : null,
                req.getExpiredAt() != null ? req.getExpiredAt().toString() : null, userId);
        addHistoryIfChanged(histories, id, "description", sw.getDescription(), req.getDescription(), userId);

        return histories;
    }

    private void addHistoryIfChanged(List<AssetSwHistory> histories, Long assetSwId,
                                      String field, String before, String after, Long userId) {
        if (!Objects.equals(before, after)) {
            histories.add(AssetSwHistory.builder()
                    .assetSwId(assetSwId)
                    .changedField(field)
                    .beforeValue(before)
                    .afterValue(after)
                    .createdBy(userId)
                    .build());
        }
    }

    private AssetSwResponse toResponse(AssetSw sw) {
        return AssetSwResponse.builder()
                .assetSwId(sw.getAssetSwId())
                .swNm(sw.getSwNm())
                .swTypeCd(sw.getSwTypeCd())
                .version(sw.getVersion())
                .licenseKey(sw.getLicenseKey())
                .licenseCnt(sw.getLicenseCnt())
                .installedAt(sw.getInstalledAt())
                .expiredAt(sw.getExpiredAt())
                .companyId(sw.getCompany() != null ? sw.getCompany().getCompanyId() : null)
                .companyNm(sw.getCompany() != null ? sw.getCompany().getCompanyNm() : null)
                .managerId(sw.getManager() != null ? sw.getManager().getUserId() : null)
                .managerNm(sw.getManager() != null ? sw.getManager().getUserNm() : null)
                .status(sw.getStatus())
                .description(sw.getDescription())
                .createdAt(sw.getCreatedAt())
                .updatedAt(sw.getUpdatedAt())
                .build();
    }

    private AssetHistoryResponse toHistoryResponse(AssetSwHistory history) {
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
