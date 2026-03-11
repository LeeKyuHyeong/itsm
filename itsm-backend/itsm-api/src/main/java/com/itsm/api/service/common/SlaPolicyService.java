package com.itsm.api.service.common;

import com.itsm.api.dto.common.SlaPolicyCreateRequest;
import com.itsm.api.dto.common.SlaPolicyResponse;
import com.itsm.api.dto.common.SlaPolicyUpdateRequest;
import com.itsm.core.domain.common.SlaPolicy;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.SlaPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SlaPolicyService {

    private final SlaPolicyRepository slaPolicyRepository;

    @Transactional(readOnly = true)
    public List<SlaPolicyResponse> getAllPolicies() {
        return slaPolicyRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SlaPolicyResponse> getPoliciesByCompany(Long companyId) {
        List<SlaPolicy> policies;
        if (companyId == null) {
            policies = slaPolicyRepository.findByCompanyIdIsNull();
        } else {
            policies = slaPolicyRepository.findByCompanyId(companyId);
        }
        return policies.stream()
                .map(this::toResponse)
                .toList();
    }

    public SlaPolicyResponse createPolicy(SlaPolicyCreateRequest req, Long currentUserId) {
        SlaPolicy slaPolicy = SlaPolicy.builder()
                .companyId(req.getCompanyId())
                .priorityCd(req.getPriorityCd())
                .deadlineHours(req.getDeadlineHours())
                .warningPct(req.getWarningPct())
                .createdBy(currentUserId)
                .build();

        SlaPolicy saved = slaPolicyRepository.save(slaPolicy);
        return toResponse(saved);
    }

    public SlaPolicyResponse updatePolicy(Long policyId, SlaPolicyUpdateRequest req, Long currentUserId) {
        SlaPolicy slaPolicy = slaPolicyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "SLA 정책을 찾을 수 없습니다."));

        slaPolicy.update(req.getDeadlineHours(), req.getWarningPct());
        return toResponse(slaPolicy);
    }

    public void changePolicyStatus(Long policyId, String isActive) {
        SlaPolicy slaPolicy = slaPolicyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "SLA 정책을 찾을 수 없습니다."));

        if ("Y".equals(isActive)) {
            slaPolicy.activate();
        } else {
            slaPolicy.deactivate();
        }
    }

    private SlaPolicyResponse toResponse(SlaPolicy slaPolicy) {
        return SlaPolicyResponse.builder()
                .policyId(slaPolicy.getPolicyId())
                .companyId(slaPolicy.getCompanyId())
                .priorityCd(slaPolicy.getPriorityCd())
                .deadlineHours(slaPolicy.getDeadlineHours())
                .warningPct(slaPolicy.getWarningPct())
                .isActive(slaPolicy.getIsActive())
                .createdAt(slaPolicy.getCreatedAt())
                .build();
    }
}
