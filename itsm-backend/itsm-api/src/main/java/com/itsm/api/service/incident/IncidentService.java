package com.itsm.api.service.incident;

import com.itsm.api.dto.incident.*;
import com.itsm.core.domain.common.SlaPolicy;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.incident.*;
import com.itsm.core.domain.user.User;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.SlaPolicyRepository;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.incident.*;
import com.itsm.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentAssetRepository incidentAssetRepository;
    private final IncidentAssigneeRepository incidentAssigneeRepository;
    private final IncidentCommentRepository incidentCommentRepository;
    private final IncidentHistoryRepository incidentHistoryRepository;
    private final IncidentReportRepository incidentReportRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SlaPolicyRepository slaPolicyRepository;

    @Transactional(readOnly = true)
    public Page<IncidentResponse> search(String keyword, Long companyId, String statusCd,
                                          String priorityCd, String incidentTypeCd, Pageable pageable) {
        return incidentRepository.search(keyword, companyId, statusCd, priorityCd, incidentTypeCd, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getDetail(Long incidentId) {
        Incident incident = findById(incidentId);
        return toResponse(incident);
    }

    public IncidentResponse create(IncidentCreateRequest req, Long currentUserId) {
        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "고객사를 찾을 수 없습니다."));

        User mainManager = null;
        if (req.getMainManagerId() != null) {
            mainManager = userRepository.findById(req.getMainManagerId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "담당자를 찾을 수 없습니다."));
        }

        Incident incident = Incident.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .incidentTypeCd(req.getIncidentTypeCd())
                .priorityCd(req.getPriorityCd())
                .occurredAt(req.getOccurredAt())
                .company(company)
                .mainManager(mainManager)
                .build();
        incident.setCreatedBy(currentUserId);

        applySlaDeadline(incident, company.getCompanyId(), req.getPriorityCd());

        Incident saved = incidentRepository.save(incident);

        if (req.getAssets() != null) {
            for (IncidentAssetRequest assetReq : req.getAssets()) {
                IncidentAsset incidentAsset = IncidentAsset.builder()
                        .incidentId(saved.getIncidentId())
                        .assetType(assetReq.getAssetType())
                        .assetId(assetReq.getAssetId())
                        .createdBy(currentUserId)
                        .build();
                incidentAssetRepository.save(incidentAsset);
            }
        }

        return toResponse(saved);
    }

    public IncidentResponse update(Long incidentId, IncidentUpdateRequest req, Long currentUserId) {
        Incident incident = findById(incidentId);

        List<IncidentHistory> histories = buildHistories(incident, req, currentUserId);
        if (!histories.isEmpty()) {
            incidentHistoryRepository.saveAll(histories);
        }

        incident.update(req.getTitle(), req.getContent(), req.getIncidentTypeCd(),
                req.getPriorityCd(), req.getOccurredAt());

        if (req.getProcessContent() != null) {
            incident.writeProcessContent(req.getProcessContent());
        }
        incident.setUpdatedBy(currentUserId);

        return toResponse(incident);
    }

    public void changeStatus(Long incidentId, String newStatus, Long currentUserId) {
        Incident incident = findById(incidentId);
        String beforeStatus = incident.getStatusCd();

        try {
            incident.changeStatus(newStatus);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION, e.getMessage());
        }

        if ("REJECTED".equals(newStatus) && incident.getSlaDeadlineAt() != null) {
            incident.extendSlaDeadline(
                    getSlaDeadlineHours(incident.getCompany().getCompanyId(), incident.getPriorityCd()));
        }

        IncidentHistory history = IncidentHistory.builder()
                .incidentId(incidentId)
                .changedField("statusCd")
                .beforeValue(beforeStatus)
                .afterValue(newStatus)
                .createdBy(currentUserId)
                .build();
        incidentHistoryRepository.save(history);
    }

    public IncidentAssigneeResponse assignUser(Long incidentId, Long userId, Long currentUserId) {
        findById(incidentId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        IncidentAssigneeId id = new IncidentAssigneeId(incidentId, userId);
        incidentAssigneeRepository.findById(id).ifPresent(existing -> {
            throw new BusinessException(ErrorCode.DUPLICATE_VALUE, "이미 지정된 담당자입니다.");
        });

        IncidentAssignee assignee = IncidentAssignee.builder()
                .incidentId(incidentId)
                .userId(userId)
                .grantedBy(currentUserId)
                .build();
        IncidentAssignee saved = incidentAssigneeRepository.save(assignee);

        return IncidentAssigneeResponse.builder()
                .incidentId(saved.getIncidentId())
                .userId(saved.getUserId())
                .userNm(user.getUserNm())
                .grantedAt(saved.getGrantedAt())
                .build();
    }

    public void removeAssignee(Long incidentId, Long userId) {
        IncidentAssigneeId id = new IncidentAssigneeId(incidentId, userId);
        IncidentAssignee assignee = incidentAssigneeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "담당자를 찾을 수 없습니다."));
        incidentAssigneeRepository.delete(assignee);
    }

    @Transactional(readOnly = true)
    public List<IncidentAssigneeResponse> getAssignees(Long incidentId) {
        return incidentAssigneeRepository.findByIncidentId(incidentId).stream()
                .map(a -> IncidentAssigneeResponse.builder()
                        .incidentId(a.getIncidentId())
                        .userId(a.getUserId())
                        .userNm(a.getUser() != null ? a.getUser().getUserNm() : null)
                        .grantedAt(a.getGrantedAt())
                        .build())
                .toList();
    }

    public IncidentCommentResponse addComment(Long incidentId, String content, Long currentUserId) {
        findById(incidentId);
        IncidentComment comment = IncidentComment.builder()
                .incidentId(incidentId)
                .content(content)
                .createdBy(currentUserId)
                .build();
        IncidentComment saved = incidentCommentRepository.save(comment);
        return toCommentResponse(saved);
    }

    public IncidentCommentResponse updateComment(Long incidentId, Long commentId,
                                                  String content, Long currentUserId) {
        IncidentComment comment = incidentCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "댓글을 찾을 수 없습니다."));
        comment.updateContent(content, currentUserId);
        return toCommentResponse(comment);
    }

    public void deleteComment(Long incidentId, Long commentId) {
        IncidentComment comment = incidentCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "댓글을 찾을 수 없습니다."));
        incidentCommentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<IncidentCommentResponse> getComments(Long incidentId) {
        return incidentCommentRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId).stream()
                .map(this::toCommentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IncidentHistoryResponse> getHistory(Long incidentId) {
        return incidentHistoryRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId).stream()
                .map(h -> IncidentHistoryResponse.builder()
                        .historyId(h.getHistoryId())
                        .changedField(h.getChangedField())
                        .beforeValue(h.getBeforeValue())
                        .afterValue(h.getAfterValue())
                        .createdBy(h.getCreatedBy())
                        .createdAt(h.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public IncidentReportResponse getReport(Long incidentId) {
        IncidentReport report = incidentReportRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "장애보고서를 찾을 수 없습니다."));
        return toReportResponse(report);
    }

    public IncidentReportResponse saveReport(Long incidentId, IncidentReportRequest req, Long currentUserId) {
        findById(incidentId);
        incidentReportRepository.findByIncidentId(incidentId).ifPresent(existing -> {
            throw new BusinessException(ErrorCode.DUPLICATE_VALUE, "이미 장애보고서가 존재합니다.");
        });

        IncidentReport report = IncidentReport.builder()
                .incidentId(incidentId)
                .reportFormId(req.getReportFormId())
                .reportContent(req.getReportContent())
                .createdBy(currentUserId)
                .build();
        IncidentReport saved = incidentReportRepository.save(report);
        return toReportResponse(saved);
    }

    public IncidentReportResponse updateReport(Long incidentId, IncidentReportRequest req, Long currentUserId) {
        IncidentReport report = incidentReportRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "장애보고서를 찾을 수 없습니다."));
        report.update(req.getReportContent(), currentUserId);
        return toReportResponse(report);
    }

    public void assignMainManager(Long incidentId, Long managerId, Long currentUserId) {
        Incident incident = findById(incidentId);
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        String beforeManager = incident.getMainManager() != null ?
                incident.getMainManager().getUserNm() : null;
        incident.assignMainManager(manager);
        incident.setUpdatedBy(currentUserId);

        IncidentHistory history = IncidentHistory.builder()
                .incidentId(incidentId)
                .changedField("mainManager")
                .beforeValue(beforeManager)
                .afterValue(manager.getUserNm())
                .createdBy(currentUserId)
                .build();
        incidentHistoryRepository.save(history);
    }

    public IncidentAssetResponse addAsset(Long incidentId, IncidentAssetRequest req, Long currentUserId) {
        findById(incidentId);
        IncidentAsset asset = IncidentAsset.builder()
                .incidentId(incidentId)
                .assetType(req.getAssetType())
                .assetId(req.getAssetId())
                .createdBy(currentUserId)
                .build();
        IncidentAsset saved = incidentAssetRepository.save(asset);
        return IncidentAssetResponse.builder()
                .incidentId(saved.getIncidentId())
                .assetType(saved.getAssetType())
                .assetId(saved.getAssetId())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public void removeAsset(Long incidentId, String assetType, Long assetId) {
        IncidentAssetId id = new IncidentAssetId(incidentId, assetType, assetId);
        IncidentAsset asset = incidentAssetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "연관 자산을 찾을 수 없습니다."));
        incidentAssetRepository.delete(asset);
    }

    @Transactional(readOnly = true)
    public List<IncidentAssetResponse> getAssets(Long incidentId) {
        return incidentAssetRepository.findByIncidentId(incidentId).stream()
                .map(a -> IncidentAssetResponse.builder()
                        .incidentId(a.getIncidentId())
                        .assetType(a.getAssetType())
                        .assetId(a.getAssetId())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();
    }

    private Incident findById(Long incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "장애를 찾을 수 없습니다."));
    }

    private void applySlaDeadline(Incident incident, Long companyId, String priorityCd) {
        int hours = getSlaDeadlineHours(companyId, priorityCd);
        if (hours > 0) {
            incident.setSlaDeadline(incident.getOccurredAt().plusHours(hours));
        }
    }

    private int getSlaDeadlineHours(Long companyId, String priorityCd) {
        return slaPolicyRepository.findByCompanyIdAndPriorityCd(companyId, priorityCd)
                .or(() -> slaPolicyRepository.findByCompanyIdIsNullAndPriorityCd(priorityCd))
                .map(SlaPolicy::getDeadlineHours)
                .orElse(0);
    }

    private Double calculateSlaPercentage(Incident incident) {
        if (incident.getSlaDeadlineAt() == null || incident.getOccurredAt() == null) {
            return null;
        }
        if ("CLOSED".equals(incident.getStatusCd()) || "COMPLETED".equals(incident.getStatusCd())) {
            LocalDateTime endTime = incident.getCompletedAt() != null ?
                    incident.getCompletedAt() : LocalDateTime.now();
            long totalMinutes = Duration.between(incident.getOccurredAt(), incident.getSlaDeadlineAt()).toMinutes();
            long elapsedMinutes = Duration.between(incident.getOccurredAt(), endTime).toMinutes();
            if (totalMinutes <= 0) return 100.0;
            return Math.min(100.0, Math.max(0.0, (double) elapsedMinutes / totalMinutes * 100));
        }
        long totalMinutes = Duration.between(incident.getOccurredAt(), incident.getSlaDeadlineAt()).toMinutes();
        long elapsedMinutes = Duration.between(incident.getOccurredAt(), LocalDateTime.now()).toMinutes();
        if (totalMinutes <= 0) return 100.0;
        return Math.min(100.0, Math.max(0.0, (double) elapsedMinutes / totalMinutes * 100));
    }

    private List<IncidentHistory> buildHistories(Incident incident, IncidentUpdateRequest req, Long userId) {
        List<IncidentHistory> histories = new ArrayList<>();
        Long id = incident.getIncidentId();

        addHistoryIfChanged(histories, id, "title", incident.getTitle(), req.getTitle(), userId);
        addHistoryIfChanged(histories, id, "content", incident.getContent(), req.getContent(), userId);
        addHistoryIfChanged(histories, id, "incidentTypeCd", incident.getIncidentTypeCd(), req.getIncidentTypeCd(), userId);
        addHistoryIfChanged(histories, id, "priorityCd", incident.getPriorityCd(), req.getPriorityCd(), userId);
        addHistoryIfChanged(histories, id, "occurredAt",
                incident.getOccurredAt() != null ? incident.getOccurredAt().toString() : null,
                req.getOccurredAt() != null ? req.getOccurredAt().toString() : null, userId);

        return histories;
    }

    private void addHistoryIfChanged(List<IncidentHistory> histories, Long incidentId,
                                      String field, String before, String after, Long userId) {
        if (!Objects.equals(before, after)) {
            histories.add(IncidentHistory.builder()
                    .incidentId(incidentId)
                    .changedField(field)
                    .beforeValue(before)
                    .afterValue(after)
                    .createdBy(userId)
                    .build());
        }
    }

    private IncidentResponse toResponse(Incident incident) {
        return IncidentResponse.builder()
                .incidentId(incident.getIncidentId())
                .title(incident.getTitle())
                .content(incident.getContent())
                .incidentTypeCd(incident.getIncidentTypeCd())
                .priorityCd(incident.getPriorityCd())
                .statusCd(incident.getStatusCd())
                .occurredAt(incident.getOccurredAt())
                .completedAt(incident.getCompletedAt())
                .closedAt(incident.getClosedAt())
                .slaDeadlineAt(incident.getSlaDeadlineAt())
                .companyId(incident.getCompany() != null ? incident.getCompany().getCompanyId() : null)
                .companyNm(incident.getCompany() != null ? incident.getCompany().getCompanyNm() : null)
                .mainManagerId(incident.getMainManager() != null ? incident.getMainManager().getUserId() : null)
                .mainManagerNm(incident.getMainManager() != null ? incident.getMainManager().getUserNm() : null)
                .processContent(incident.getProcessContent())
                .slaPercentage(calculateSlaPercentage(incident))
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }

    private IncidentCommentResponse toCommentResponse(IncidentComment comment) {
        return IncidentCommentResponse.builder()
                .commentId(comment.getCommentId())
                .incidentId(comment.getIncidentId())
                .content(comment.getContent())
                .createdBy(comment.getCreatedBy())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private IncidentReportResponse toReportResponse(IncidentReport report) {
        return IncidentReportResponse.builder()
                .reportId(report.getReportId())
                .incidentId(report.getIncidentId())
                .reportFormId(report.getReportFormId())
                .reportContent(report.getReportContent())
                .createdBy(report.getCreatedBy())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
