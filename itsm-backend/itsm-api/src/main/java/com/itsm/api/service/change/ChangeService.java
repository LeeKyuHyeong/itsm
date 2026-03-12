package com.itsm.api.service.change;

import com.itsm.api.dto.change.*;
import com.itsm.core.domain.change.*;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.change.*;
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
public class ChangeService {

    private final ChangeRepository changeRepository;
    private final ChangeAssetRepository changeAssetRepository;
    private final ChangeApproverRepository changeApproverRepository;
    private final ChangeHistoryRepository changeHistoryRepository;
    private final ChangeCommentRepository changeCommentRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ChangeResponse> search(String keyword, Long companyId, String statusCd,
                                        String priorityCd, String changeTypeCd, Pageable pageable) {
        return changeRepository.search(keyword, companyId, statusCd, priorityCd, changeTypeCd, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ChangeResponse getDetail(Long changeId) {
        Change change = findById(changeId);
        List<ChangeApprover> approvers = changeApproverRepository.findByChangeIdOrderByApproveOrderAsc(changeId);
        return ChangeResponse.builder()
                .changeId(change.getChangeId())
                .title(change.getTitle())
                .content(change.getContent())
                .changeTypeCd(change.getChangeTypeCd())
                .priorityCd(change.getPriorityCd())
                .statusCd(change.getStatusCd())
                .scheduledAt(change.getScheduledAt())
                .rollbackPlan(change.getRollbackPlan())
                .companyId(change.getCompany() != null ? change.getCompany().getCompanyId() : null)
                .companyNm(change.getCompany() != null ? change.getCompany().getCompanyNm() : null)
                .approverCount(approvers.size())
                .approvedCount((int) approvers.stream()
                        .filter(a -> "APPROVED".equals(a.getApproveStatus())).count())
                .createdAt(change.getCreatedAt())
                .updatedAt(change.getUpdatedAt())
                .build();
    }

    public ChangeResponse create(ChangeCreateRequest req, Long currentUserId) {
        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "고객사를 찾을 수 없습니다."));

        Change change = Change.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .changeTypeCd(req.getChangeTypeCd())
                .priorityCd(req.getPriorityCd())
                .scheduledAt(req.getScheduledAt())
                .rollbackPlan(req.getRollbackPlan())
                .company(company)
                .build();
        change.setCreatedBy(currentUserId);

        Change saved = changeRepository.save(change);
        return toResponse(saved);
    }

    public ChangeResponse update(Long changeId, ChangeUpdateRequest req, Long currentUserId) {
        Change change = findById(changeId);

        List<ChangeHistory> histories = buildHistories(change, req, currentUserId);
        if (!histories.isEmpty()) {
            changeHistoryRepository.saveAll(histories);
        }

        change.update(req.getTitle(), req.getContent(), req.getChangeTypeCd(),
                req.getPriorityCd(), req.getScheduledAt(), req.getRollbackPlan());
        change.setUpdatedBy(currentUserId);

        return toResponse(change);
    }

    public void changeStatus(Long changeId, String newStatus, Long currentUserId) {
        Change change = findById(changeId);
        String beforeStatus = change.getStatusCd();

        try {
            change.changeStatus(newStatus);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION, e.getMessage());
        }

        ChangeHistory history = ChangeHistory.builder()
                .changeId(changeId)
                .changedField("statusCd")
                .beforeValue(beforeStatus)
                .afterValue(newStatus)
                .createdBy(currentUserId)
                .build();
        changeHistoryRepository.save(history);
    }

    public ChangeApproverResponse addApprover(Long changeId, Long userId, Long currentUserId) {
        findById(changeId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        ChangeApproverId id = new ChangeApproverId(changeId, userId);
        changeApproverRepository.findById(id).ifPresent(existing -> {
            throw new BusinessException(ErrorCode.DUPLICATE_VALUE, "이미 지정된 승인자입니다.");
        });

        List<ChangeApprover> existing = changeApproverRepository.findByChangeIdOrderByApproveOrderAsc(changeId);
        int nextOrder = existing.isEmpty() ? 1 : existing.get(existing.size() - 1).getApproveOrder() + 1;

        ChangeApprover approver = ChangeApprover.builder()
                .changeId(changeId)
                .userId(userId)
                .approveOrder(nextOrder)
                .build();
        ChangeApprover saved = changeApproverRepository.save(approver);

        return ChangeApproverResponse.builder()
                .changeId(saved.getChangeId())
                .userId(saved.getUserId())
                .userNm(user.getUserNm())
                .approveOrder(saved.getApproveOrder())
                .approveStatus(saved.getApproveStatus())
                .build();
    }

    public void removeApprover(Long changeId, Long userId) {
        ChangeApproverId id = new ChangeApproverId(changeId, userId);
        ChangeApprover approver = changeApproverRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "승인자를 찾을 수 없습니다."));
        changeApproverRepository.delete(approver);
    }

    @Transactional(readOnly = true)
    public List<ChangeApproverResponse> getApprovers(Long changeId) {
        return changeApproverRepository.findByChangeIdOrderByApproveOrderAsc(changeId).stream()
                .map(a -> ChangeApproverResponse.builder()
                        .changeId(a.getChangeId())
                        .userId(a.getUserId())
                        .userNm(a.getUser() != null ? a.getUser().getUserNm() : null)
                        .approveOrder(a.getApproveOrder())
                        .approveStatus(a.getApproveStatus())
                        .approvedAt(a.getApprovedAt())
                        .comment(a.getComment())
                        .build())
                .toList();
    }

    public void approveChange(Long changeId, Long userId, String decision, String comment, Long currentUserId) {
        Change change = findById(changeId);
        if (!"APPROVAL_REQUESTED".equals(change.getStatusCd())) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION, "승인요청 상태에서만 승인/반려가 가능합니다.");
        }

        List<ChangeApprover> approvers = changeApproverRepository.findByChangeIdOrderByApproveOrderAsc(changeId);
        ChangeApprover target = approvers.stream()
                .filter(a -> a.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "승인자를 찾을 수 없습니다."));

        // 순차 승인 검증: 이전 순서의 승인자가 모두 승인되었는지 확인
        boolean previousAllApproved = approvers.stream()
                .filter(a -> a.getApproveOrder() < target.getApproveOrder())
                .allMatch(a -> "APPROVED".equals(a.getApproveStatus()));
        if (!previousAllApproved) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION, "이전 순서 승인자의 승인이 필요합니다.");
        }

        if ("REJECTED".equals(decision)) {
            target.reject(comment);
            change.changeStatus("REJECTED");

            ChangeHistory history = ChangeHistory.builder()
                    .changeId(changeId).changedField("statusCd")
                    .beforeValue("APPROVAL_REQUESTED").afterValue("REJECTED")
                    .createdBy(currentUserId).build();
            changeHistoryRepository.save(history);
        } else {
            target.approve(comment);

            // 전원 승인 시 자동 APPROVED 전이
            boolean allApproved = approvers.stream()
                    .allMatch(a -> "APPROVED".equals(a.getApproveStatus()));
            if (allApproved) {
                change.changeStatus("APPROVED");

                ChangeHistory history = ChangeHistory.builder()
                        .changeId(changeId).changedField("statusCd")
                        .beforeValue("APPROVAL_REQUESTED").afterValue("APPROVED")
                        .createdBy(currentUserId).build();
                changeHistoryRepository.save(history);
            }
        }
    }

    public ChangeCommentResponse addComment(Long changeId, String content, Long currentUserId) {
        findById(changeId);
        ChangeComment comment = ChangeComment.builder()
                .changeId(changeId)
                .content(content)
                .createdBy(currentUserId)
                .build();
        ChangeComment saved = changeCommentRepository.save(comment);
        return toCommentResponse(saved);
    }

    public ChangeCommentResponse updateComment(Long changeId, Long commentId,
                                                String content, Long currentUserId) {
        ChangeComment comment = changeCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "댓글을 찾을 수 없습니다."));
        comment.updateContent(content, currentUserId);
        return toCommentResponse(comment);
    }

    public void deleteComment(Long changeId, Long commentId) {
        ChangeComment comment = changeCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "댓글을 찾을 수 없습니다."));
        changeCommentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<ChangeCommentResponse> getComments(Long changeId) {
        return changeCommentRepository.findByChangeIdOrderByCreatedAtAsc(changeId).stream()
                .map(this::toCommentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChangeHistoryResponse> getHistory(Long changeId) {
        return changeHistoryRepository.findByChangeIdOrderByCreatedAtDesc(changeId).stream()
                .map(h -> ChangeHistoryResponse.builder()
                        .historyId(h.getHistoryId())
                        .changedField(h.getChangedField())
                        .beforeValue(h.getBeforeValue())
                        .afterValue(h.getAfterValue())
                        .createdBy(h.getCreatedBy())
                        .createdAt(h.getCreatedAt())
                        .build())
                .toList();
    }

    private Change findById(Long changeId) {
        return changeRepository.findById(changeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "변경요청을 찾을 수 없습니다."));
    }

    private List<ChangeHistory> buildHistories(Change change, ChangeUpdateRequest req, Long userId) {
        List<ChangeHistory> histories = new ArrayList<>();
        Long id = change.getChangeId();

        addHistoryIfChanged(histories, id, "title", change.getTitle(), req.getTitle(), userId);
        addHistoryIfChanged(histories, id, "content", change.getContent(), req.getContent(), userId);
        addHistoryIfChanged(histories, id, "changeTypeCd", change.getChangeTypeCd(), req.getChangeTypeCd(), userId);
        addHistoryIfChanged(histories, id, "priorityCd", change.getPriorityCd(), req.getPriorityCd(), userId);
        addHistoryIfChanged(histories, id, "scheduledAt",
                change.getScheduledAt() != null ? change.getScheduledAt().toString() : null,
                req.getScheduledAt() != null ? req.getScheduledAt().toString() : null, userId);
        addHistoryIfChanged(histories, id, "rollbackPlan", change.getRollbackPlan(), req.getRollbackPlan(), userId);

        return histories;
    }

    private void addHistoryIfChanged(List<ChangeHistory> histories, Long changeId,
                                      String field, String before, String after, Long userId) {
        if (!Objects.equals(before, after)) {
            histories.add(ChangeHistory.builder()
                    .changeId(changeId)
                    .changedField(field)
                    .beforeValue(before)
                    .afterValue(after)
                    .createdBy(userId)
                    .build());
        }
    }

    private ChangeResponse toResponse(Change change) {
        return ChangeResponse.builder()
                .changeId(change.getChangeId())
                .title(change.getTitle())
                .content(change.getContent())
                .changeTypeCd(change.getChangeTypeCd())
                .priorityCd(change.getPriorityCd())
                .statusCd(change.getStatusCd())
                .scheduledAt(change.getScheduledAt())
                .rollbackPlan(change.getRollbackPlan())
                .companyId(change.getCompany() != null ? change.getCompany().getCompanyId() : null)
                .companyNm(change.getCompany() != null ? change.getCompany().getCompanyNm() : null)
                .createdAt(change.getCreatedAt())
                .updatedAt(change.getUpdatedAt())
                .build();
    }

    private ChangeCommentResponse toCommentResponse(ChangeComment comment) {
        return ChangeCommentResponse.builder()
                .commentId(comment.getCommentId())
                .changeId(comment.getChangeId())
                .content(comment.getContent())
                .createdBy(comment.getCreatedBy())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
