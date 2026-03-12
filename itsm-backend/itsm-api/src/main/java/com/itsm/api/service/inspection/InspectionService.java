package com.itsm.api.service.inspection;

import com.itsm.api.dto.inspection.*;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.inspection.*;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.inspection.*;
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
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final InspectionItemRepository inspectionItemRepository;
    private final InspectionResultRepository inspectionResultRepository;
    private final InspectionHistoryRepository inspectionHistoryRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public Page<InspectionResponse> search(String keyword, Long companyId, String statusCd,
                                            String inspectionTypeCd, Pageable pageable) {
        return inspectionRepository.search(keyword, companyId, statusCd, inspectionTypeCd, pageable)
                .map(this::toResponseWithCounts);
    }

    @Transactional(readOnly = true)
    public InspectionResponse getDetail(Long inspectionId) {
        Inspection inspection = findById(inspectionId);
        return toResponseWithCounts(inspection);
    }

    public InspectionResponse create(InspectionCreateRequest req, Long currentUserId) {
        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "고객사를 찾을 수 없습니다."));

        Inspection inspection = Inspection.builder()
                .title(req.getTitle())
                .inspectionTypeCd(req.getInspectionTypeCd())
                .scheduledAt(req.getScheduledAt())
                .company(company)
                .managerId(req.getManagerId())
                .description(req.getDescription())
                .build();
        inspection.setCreatedBy(currentUserId);

        Inspection saved = inspectionRepository.save(inspection);
        return toResponse(saved);
    }

    public InspectionResponse update(Long inspectionId, InspectionUpdateRequest req, Long currentUserId) {
        Inspection inspection = findById(inspectionId);

        List<InspectionHistory> histories = buildHistories(inspection, req, currentUserId);
        if (!histories.isEmpty()) {
            inspectionHistoryRepository.saveAll(histories);
        }

        inspection.update(req.getTitle(), req.getInspectionTypeCd(), req.getScheduledAt(), req.getDescription());
        if (req.getManagerId() != null) {
            inspection.updateManager(req.getManagerId());
        }
        inspection.setUpdatedBy(currentUserId);

        return toResponseWithCounts(inspection);
    }

    public void changeStatus(Long inspectionId, String newStatus, Long currentUserId) {
        Inspection inspection = findById(inspectionId);
        String beforeStatus = inspection.getStatusCd();

        try {
            inspection.changeStatus(newStatus);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION, e.getMessage());
        }

        InspectionHistory history = InspectionHistory.builder()
                .inspectionId(inspectionId)
                .changedField("statusCd")
                .beforeValue(beforeStatus)
                .afterValue(newStatus)
                .createdBy(currentUserId)
                .build();
        inspectionHistoryRepository.save(history);
    }

    public InspectionItemResponse addItem(Long inspectionId, InspectionItemRequest req, Long currentUserId) {
        findById(inspectionId);

        InspectionItem item = InspectionItem.builder()
                .inspectionId(inspectionId)
                .itemNm(req.getItemNm())
                .sortOrder(req.getSortOrder())
                .isRequired(req.getIsRequired())
                .createdBy(currentUserId)
                .build();
        InspectionItem saved = inspectionItemRepository.save(item);

        return toItemResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InspectionItemResponse> getItems(Long inspectionId) {
        return inspectionItemRepository.findByInspectionIdOrderBySortOrderAsc(inspectionId).stream()
                .map(this::toItemResponse)
                .toList();
    }

    public void deleteItem(Long inspectionId, Long itemId) {
        InspectionItem item = inspectionItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "점검항목을 찾을 수 없습니다."));
        inspectionItemRepository.delete(item);
    }

    public InspectionResultResponse addResult(Long inspectionId, InspectionResultRequest req, Long currentUserId) {
        findById(inspectionId);
        InspectionItem item = inspectionItemRepository.findById(req.getItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "점검항목을 찾을 수 없습니다."));

        InspectionResult result = inspectionResultRepository.findByInspectionIdAndItemId(inspectionId, req.getItemId())
                .map(existing -> {
                    existing.update(req.getResultValue(), req.getIsNormal(), req.getRemark());
                    return existing;
                })
                .orElseGet(() -> {
                    InspectionResult newResult = InspectionResult.builder()
                            .inspectionId(inspectionId)
                            .itemId(req.getItemId())
                            .resultValue(req.getResultValue())
                            .isNormal(req.getIsNormal())
                            .remark(req.getRemark())
                            .build();
                    newResult.setCreatedBy(currentUserId);
                    return inspectionResultRepository.save(newResult);
                });

        return toResultResponse(result, item.getItemNm());
    }

    @Transactional(readOnly = true)
    public List<InspectionResultResponse> getResults(Long inspectionId) {
        return inspectionResultRepository.findByInspectionId(inspectionId).stream()
                .map(r -> {
                    String itemNm = inspectionItemRepository.findById(r.getItemId())
                            .map(InspectionItem::getItemNm)
                            .orElse(null);
                    return toResultResponse(r, itemNm);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InspectionHistoryResponse> getHistory(Long inspectionId) {
        return inspectionHistoryRepository.findByInspectionIdOrderByCreatedAtDesc(inspectionId).stream()
                .map(h -> InspectionHistoryResponse.builder()
                        .historyId(h.getHistoryId())
                        .changedField(h.getChangedField())
                        .beforeValue(h.getBeforeValue())
                        .afterValue(h.getAfterValue())
                        .createdBy(h.getCreatedBy())
                        .createdAt(h.getCreatedAt())
                        .build())
                .toList();
    }

    private Inspection findById(Long inspectionId) {
        return inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "점검을 찾을 수 없습니다."));
    }

    private InspectionResponse toResponse(Inspection inspection) {
        return InspectionResponse.builder()
                .inspectionId(inspection.getInspectionId())
                .title(inspection.getTitle())
                .inspectionTypeCd(inspection.getInspectionTypeCd())
                .statusCd(inspection.getStatusCd())
                .scheduledAt(inspection.getScheduledAt())
                .completedAt(inspection.getCompletedAt())
                .closedAt(inspection.getClosedAt())
                .companyId(inspection.getCompany() != null ? inspection.getCompany().getCompanyId() : null)
                .companyNm(inspection.getCompany() != null ? inspection.getCompany().getCompanyNm() : null)
                .managerId(inspection.getManagerId())
                .description(inspection.getDescription())
                .createdAt(inspection.getCreatedAt())
                .updatedAt(inspection.getUpdatedAt())
                .build();
    }

    private InspectionResponse toResponseWithCounts(Inspection inspection) {
        long itemCount = inspectionItemRepository.countByInspectionId(inspection.getInspectionId());
        long completedItemCount = inspectionResultRepository.countByInspectionId(inspection.getInspectionId());

        return InspectionResponse.builder()
                .inspectionId(inspection.getInspectionId())
                .title(inspection.getTitle())
                .inspectionTypeCd(inspection.getInspectionTypeCd())
                .statusCd(inspection.getStatusCd())
                .scheduledAt(inspection.getScheduledAt())
                .completedAt(inspection.getCompletedAt())
                .closedAt(inspection.getClosedAt())
                .companyId(inspection.getCompany() != null ? inspection.getCompany().getCompanyId() : null)
                .companyNm(inspection.getCompany() != null ? inspection.getCompany().getCompanyNm() : null)
                .managerId(inspection.getManagerId())
                .description(inspection.getDescription())
                .itemCount((int) itemCount)
                .completedItemCount((int) completedItemCount)
                .createdAt(inspection.getCreatedAt())
                .updatedAt(inspection.getUpdatedAt())
                .build();
    }

    private InspectionItemResponse toItemResponse(InspectionItem item) {
        return InspectionItemResponse.builder()
                .itemId(item.getItemId())
                .itemNm(item.getItemNm())
                .sortOrder(item.getSortOrder())
                .isRequired(item.getIsRequired())
                .build();
    }

    private InspectionResultResponse toResultResponse(InspectionResult result, String itemNm) {
        return InspectionResultResponse.builder()
                .resultId(result.getResultId())
                .itemId(result.getItemId())
                .itemNm(itemNm)
                .resultValue(result.getResultValue())
                .isNormal(result.getIsNormal())
                .remark(result.getRemark())
                .build();
    }

    private List<InspectionHistory> buildHistories(Inspection inspection, InspectionUpdateRequest req, Long userId) {
        List<InspectionHistory> histories = new ArrayList<>();
        Long id = inspection.getInspectionId();

        addHistoryIfChanged(histories, id, "title", inspection.getTitle(), req.getTitle(), userId);
        addHistoryIfChanged(histories, id, "inspectionTypeCd", inspection.getInspectionTypeCd(), req.getInspectionTypeCd(), userId);
        addHistoryIfChanged(histories, id, "scheduledAt",
                inspection.getScheduledAt() != null ? inspection.getScheduledAt().toString() : null,
                req.getScheduledAt() != null ? req.getScheduledAt().toString() : null, userId);
        addHistoryIfChanged(histories, id, "description", inspection.getDescription(), req.getDescription(), userId);

        return histories;
    }

    private void addHistoryIfChanged(List<InspectionHistory> histories, Long inspectionId,
                                      String field, String before, String after, Long userId) {
        if (!Objects.equals(before, after)) {
            histories.add(InspectionHistory.builder()
                    .inspectionId(inspectionId)
                    .changedField(field)
                    .beforeValue(before)
                    .afterValue(after)
                    .createdBy(userId)
                    .build());
        }
    }
}
