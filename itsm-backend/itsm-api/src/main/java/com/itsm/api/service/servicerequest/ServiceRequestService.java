package com.itsm.api.service.servicerequest;

import com.itsm.api.dto.servicerequest.*;
import com.itsm.core.domain.common.SlaPolicy;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.servicerequest.*;
import com.itsm.core.domain.user.User;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.SlaPolicyRepository;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.servicerequest.*;
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
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestAssigneeRepository assigneeRepository;
    private final ServiceRequestProcessRepository processRepository;
    private final ServiceRequestHistoryRepository historyRepository;
    private final ServiceRequestAssetRepository assetRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SlaPolicyRepository slaPolicyRepository;

    @Transactional(readOnly = true)
    public Page<SrResponse> search(String keyword, Long companyId, String statusCd,
                                    String priorityCd, String requestTypeCd, Pageable pageable) {
        return serviceRequestRepository.search(keyword, companyId, statusCd, priorityCd, requestTypeCd, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public SrResponse getDetail(Long requestId) {
        ServiceRequest sr = findById(requestId);
        SrResponse response = toResponse(sr);
        List<ServiceRequestAssignee> assignees = assigneeRepository.findByRequestId(requestId);
        return SrResponse.builder()
                .requestId(sr.getRequestId())
                .title(sr.getTitle())
                .content(sr.getContent())
                .requestTypeCd(sr.getRequestTypeCd())
                .priorityCd(sr.getPriorityCd())
                .statusCd(sr.getStatusCd())
                .occurredAt(sr.getOccurredAt())
                .completedAt(sr.getCompletedAt())
                .closedAt(sr.getClosedAt())
                .slaDeadlineAt(sr.getSlaDeadlineAt())
                .rejectCnt(sr.getRejectCnt())
                .companyId(sr.getCompany() != null ? sr.getCompany().getCompanyId() : null)
                .companyNm(sr.getCompany() != null ? sr.getCompany().getCompanyNm() : null)
                .satisfactionScore(sr.getSatisfactionScore())
                .satisfactionComment(sr.getSatisfactionComment())
                .slaPercentage(calculateSlaPercentage(sr))
                .assigneeCount(assignees.size())
                .completedAssigneeCount((int) assignees.stream()
                        .filter(a -> "COMPLETED".equals(a.getProcessStatus())).count())
                .createdAt(sr.getCreatedAt())
                .updatedAt(sr.getUpdatedAt())
                .build();
    }

    public SrResponse create(SrCreateRequest req, Long currentUserId) {
        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "고객사를 찾을 수 없습니다."));

        ServiceRequest sr = ServiceRequest.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .requestTypeCd(req.getRequestTypeCd())
                .priorityCd(req.getPriorityCd())
                .occurredAt(req.getOccurredAt())
                .company(company)
                .build();
        sr.setCreatedBy(currentUserId);

        applySlaDeadline(sr, company.getCompanyId(), req.getPriorityCd());

        ServiceRequest saved = serviceRequestRepository.save(sr);
        return toResponse(saved);
    }

    public SrResponse update(Long requestId, SrUpdateRequest req, Long currentUserId) {
        ServiceRequest sr = findById(requestId);

        List<ServiceRequestHistory> histories = buildHistories(sr, req, currentUserId);
        if (!histories.isEmpty()) {
            historyRepository.saveAll(histories);
        }

        sr.update(req.getTitle(), req.getContent(), req.getRequestTypeCd(),
                req.getPriorityCd(), req.getOccurredAt());
        sr.setUpdatedBy(currentUserId);

        return toResponse(sr);
    }

    public void changeStatus(Long requestId, String newStatus, Long currentUserId) {
        ServiceRequest sr = findById(requestId);
        String beforeStatus = sr.getStatusCd();

        try {
            sr.changeStatus(newStatus);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION, e.getMessage());
        }

        if ("REJECTED".equals(newStatus) && sr.getSlaDeadlineAt() != null) {
            sr.extendSlaDeadline(getSlaDeadlineHours(sr.getCompany().getCompanyId(), sr.getPriorityCd()));
        }

        ServiceRequestHistory history = ServiceRequestHistory.builder()
                .requestId(requestId)
                .changedField("statusCd")
                .beforeValue(beforeStatus)
                .afterValue(newStatus)
                .createdBy(currentUserId)
                .build();
        historyRepository.save(history);
    }

    public SrAssigneeResponse assignUser(Long requestId, Long userId, Long currentUserId) {
        findById(requestId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        ServiceRequestAssigneeId id = new ServiceRequestAssigneeId(requestId, userId);
        assigneeRepository.findById(id).ifPresent(existing -> {
            throw new BusinessException(ErrorCode.DUPLICATE_VALUE, "이미 지정된 담당자입니다.");
        });

        ServiceRequestAssignee assignee = ServiceRequestAssignee.builder()
                .requestId(requestId)
                .userId(userId)
                .grantedBy(currentUserId)
                .build();
        ServiceRequestAssignee saved = assigneeRepository.save(assignee);

        return SrAssigneeResponse.builder()
                .requestId(saved.getRequestId())
                .userId(saved.getUserId())
                .userNm(user.getUserNm())
                .processStatus(saved.getProcessStatus())
                .grantedAt(saved.getGrantedAt())
                .build();
    }

    public void removeAssignee(Long requestId, Long userId) {
        ServiceRequestAssigneeId id = new ServiceRequestAssigneeId(requestId, userId);
        ServiceRequestAssignee assignee = assigneeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "담당자를 찾을 수 없습니다."));
        assigneeRepository.delete(assignee);
    }

    @Transactional(readOnly = true)
    public List<SrAssigneeResponse> getAssignees(Long requestId) {
        return assigneeRepository.findByRequestId(requestId).stream()
                .map(a -> SrAssigneeResponse.builder()
                        .requestId(a.getRequestId())
                        .userId(a.getUserId())
                        .userNm(a.getUser() != null ? a.getUser().getUserNm() : null)
                        .processStatus(a.getProcessStatus())
                        .grantedAt(a.getGrantedAt())
                        .build())
                .toList();
    }

    public SrProcessResponse addProcess(Long requestId, Long userId, String processContent) {
        findById(requestId);
        ServiceRequestProcess process = ServiceRequestProcess.builder()
                .requestId(requestId)
                .userId(userId)
                .processContent(processContent)
                .build();
        ServiceRequestProcess saved = processRepository.save(process);
        return toProcessResponse(saved);
    }

    public void completeProcess(Long requestId, Long processId) {
        ServiceRequestProcess process = processRepository.findById(processId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "처리내용을 찾을 수 없습니다."));
        process.complete();
    }

    @Transactional(readOnly = true)
    public List<SrProcessResponse> getProcesses(Long requestId) {
        return processRepository.findByRequestIdOrderByCreatedAtAsc(requestId).stream()
                .map(this::toProcessResponse)
                .toList();
    }

    public void submitSatisfaction(Long requestId, int score, String comment) {
        ServiceRequest sr = findById(requestId);
        if (!"CLOSED".equals(sr.getStatusCd())) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION, "종료된 요청만 만족도를 제출할 수 있습니다.");
        }
        sr.submitSatisfaction(score, comment);
    }

    @Transactional(readOnly = true)
    public List<SrHistoryResponse> getHistory(Long requestId) {
        return historyRepository.findByRequestIdOrderByCreatedAtDesc(requestId).stream()
                .map(h -> SrHistoryResponse.builder()
                        .historyId(h.getHistoryId())
                        .changedField(h.getChangedField())
                        .beforeValue(h.getBeforeValue())
                        .afterValue(h.getAfterValue())
                        .createdBy(h.getCreatedBy())
                        .createdAt(h.getCreatedAt())
                        .build())
                .toList();
    }

    public void checkAutoTransition(Long requestId, Long currentUserId) {
        ServiceRequest sr = findById(requestId);
        if (!"IN_PROGRESS".equals(sr.getStatusCd())) {
            return;
        }
        List<ServiceRequestAssignee> assignees = assigneeRepository.findByRequestId(requestId);
        if (assignees.isEmpty()) {
            return;
        }
        boolean allCompleted = assignees.stream()
                .allMatch(a -> "COMPLETED".equals(a.getProcessStatus()));
        if (allCompleted) {
            changeStatus(requestId, "PENDING_COMPLETE", currentUserId);
        }
    }

    private ServiceRequest findById(Long requestId) {
        return serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "서비스요청을 찾을 수 없습니다."));
    }

    private void applySlaDeadline(ServiceRequest sr, Long companyId, String priorityCd) {
        int hours = getSlaDeadlineHours(companyId, priorityCd);
        if (hours > 0) {
            sr.setSlaDeadline(sr.getOccurredAt().plusHours(hours));
        }
    }

    private int getSlaDeadlineHours(Long companyId, String priorityCd) {
        return slaPolicyRepository.findByCompanyIdAndPriorityCd(companyId, priorityCd)
                .or(() -> slaPolicyRepository.findByCompanyIdIsNullAndPriorityCd(priorityCd))
                .map(SlaPolicy::getDeadlineHours)
                .orElse(0);
    }

    private Double calculateSlaPercentage(ServiceRequest sr) {
        if (sr.getSlaDeadlineAt() == null || sr.getOccurredAt() == null) {
            return null;
        }
        if ("CLOSED".equals(sr.getStatusCd()) || "PENDING_COMPLETE".equals(sr.getStatusCd())) {
            LocalDateTime endTime = sr.getCompletedAt() != null ? sr.getCompletedAt() : LocalDateTime.now();
            long totalMinutes = Duration.between(sr.getOccurredAt(), sr.getSlaDeadlineAt()).toMinutes();
            long elapsedMinutes = Duration.between(sr.getOccurredAt(), endTime).toMinutes();
            if (totalMinutes <= 0) return 100.0;
            return Math.min(100.0, Math.max(0.0, (double) elapsedMinutes / totalMinutes * 100));
        }
        long totalMinutes = Duration.between(sr.getOccurredAt(), sr.getSlaDeadlineAt()).toMinutes();
        long elapsedMinutes = Duration.between(sr.getOccurredAt(), LocalDateTime.now()).toMinutes();
        if (totalMinutes <= 0) return 100.0;
        return Math.min(100.0, Math.max(0.0, (double) elapsedMinutes / totalMinutes * 100));
    }

    private List<ServiceRequestHistory> buildHistories(ServiceRequest sr, SrUpdateRequest req, Long userId) {
        List<ServiceRequestHistory> histories = new ArrayList<>();
        Long id = sr.getRequestId();

        addHistoryIfChanged(histories, id, "title", sr.getTitle(), req.getTitle(), userId);
        addHistoryIfChanged(histories, id, "content", sr.getContent(), req.getContent(), userId);
        addHistoryIfChanged(histories, id, "requestTypeCd", sr.getRequestTypeCd(), req.getRequestTypeCd(), userId);
        addHistoryIfChanged(histories, id, "priorityCd", sr.getPriorityCd(), req.getPriorityCd(), userId);
        addHistoryIfChanged(histories, id, "occurredAt",
                sr.getOccurredAt() != null ? sr.getOccurredAt().toString() : null,
                req.getOccurredAt() != null ? req.getOccurredAt().toString() : null, userId);

        return histories;
    }

    private void addHistoryIfChanged(List<ServiceRequestHistory> histories, Long requestId,
                                      String field, String before, String after, Long userId) {
        if (!Objects.equals(before, after)) {
            histories.add(ServiceRequestHistory.builder()
                    .requestId(requestId)
                    .changedField(field)
                    .beforeValue(before)
                    .afterValue(after)
                    .createdBy(userId)
                    .build());
        }
    }

    private SrResponse toResponse(ServiceRequest sr) {
        return SrResponse.builder()
                .requestId(sr.getRequestId())
                .title(sr.getTitle())
                .content(sr.getContent())
                .requestTypeCd(sr.getRequestTypeCd())
                .priorityCd(sr.getPriorityCd())
                .statusCd(sr.getStatusCd())
                .occurredAt(sr.getOccurredAt())
                .completedAt(sr.getCompletedAt())
                .closedAt(sr.getClosedAt())
                .slaDeadlineAt(sr.getSlaDeadlineAt())
                .rejectCnt(sr.getRejectCnt())
                .companyId(sr.getCompany() != null ? sr.getCompany().getCompanyId() : null)
                .companyNm(sr.getCompany() != null ? sr.getCompany().getCompanyNm() : null)
                .satisfactionScore(sr.getSatisfactionScore())
                .satisfactionComment(sr.getSatisfactionComment())
                .slaPercentage(calculateSlaPercentage(sr))
                .createdAt(sr.getCreatedAt())
                .updatedAt(sr.getUpdatedAt())
                .build();
    }

    private SrProcessResponse toProcessResponse(ServiceRequestProcess process) {
        return SrProcessResponse.builder()
                .processId(process.getProcessId())
                .requestId(process.getRequestId())
                .userId(process.getUserId())
                .processContent(process.getProcessContent())
                .isCompleted(process.getIsCompleted())
                .completedAt(process.getCompletedAt())
                .createdAt(process.getCreatedAt())
                .updatedAt(process.getUpdatedAt())
                .build();
    }
}
