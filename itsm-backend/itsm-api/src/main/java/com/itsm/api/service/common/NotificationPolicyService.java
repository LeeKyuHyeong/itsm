package com.itsm.api.service.common;

import com.itsm.api.dto.common.NotificationPolicyCreateRequest;
import com.itsm.api.dto.common.NotificationPolicyResponse;
import com.itsm.api.dto.common.NotificationPolicyUpdateRequest;
import com.itsm.core.domain.common.NotificationPolicy;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.NotificationPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationPolicyService {

    private final NotificationPolicyRepository notificationPolicyRepository;

    @Transactional(readOnly = true)
    public List<NotificationPolicyResponse> getAllPolicies() {
        return notificationPolicyRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationPolicyResponse> getPoliciesByType(String notiTypeCd) {
        return notificationPolicyRepository.findByNotiTypeCd(notiTypeCd).stream()
                .map(this::toResponse)
                .toList();
    }

    public NotificationPolicyResponse createPolicy(NotificationPolicyCreateRequest req, Long currentUserId) {
        NotificationPolicy policy = NotificationPolicy.builder()
                .notiTypeCd(req.getNotiTypeCd())
                .triggerCondition(req.getTriggerCondition())
                .targetRoleCd(req.getTargetRoleCd())
                .createdBy(currentUserId)
                .build();

        NotificationPolicy saved = notificationPolicyRepository.save(policy);
        return toResponse(saved);
    }

    public NotificationPolicyResponse updatePolicy(Long policyId, NotificationPolicyUpdateRequest req, Long currentUserId) {
        NotificationPolicy policy = notificationPolicyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "알림 정책을 찾을 수 없습니다."));

        policy.update(req.getTriggerCondition(), req.getTargetRoleCd());
        return toResponse(policy);
    }

    public void changePolicyStatus(Long policyId, String isActive) {
        NotificationPolicy policy = notificationPolicyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "알림 정책을 찾을 수 없습니다."));

        if ("Y".equals(isActive)) {
            policy.activate();
        } else {
            policy.deactivate();
        }
    }

    private NotificationPolicyResponse toResponse(NotificationPolicy policy) {
        return NotificationPolicyResponse.builder()
                .policyId(policy.getPolicyId())
                .notiTypeCd(policy.getNotiTypeCd())
                .triggerCondition(policy.getTriggerCondition())
                .targetRoleCd(policy.getTargetRoleCd())
                .isActive(policy.getIsActive())
                .createdAt(policy.getCreatedAt())
                .build();
    }
}
