package com.itsm.api.controller.common;

import com.itsm.api.dto.common.NotificationPolicyCreateRequest;
import com.itsm.api.dto.common.NotificationPolicyResponse;
import com.itsm.api.dto.common.NotificationPolicyUpdateRequest;
import com.itsm.api.service.common.NotificationPolicyService;
import com.itsm.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/notification-policies")
@RequiredArgsConstructor
public class NotificationPolicyController {

    private final NotificationPolicyService notificationPolicyService;

    @GetMapping
    public ApiResponse<List<NotificationPolicyResponse>> getPolicies(
            @RequestParam(required = false) String notiTypeCd) {
        if (StringUtils.hasText(notiTypeCd)) {
            return ApiResponse.success(notificationPolicyService.getPoliciesByType(notiTypeCd));
        }
        return ApiResponse.success(notificationPolicyService.getAllPolicies());
    }

    @PostMapping
    public ApiResponse<NotificationPolicyResponse> createPolicy(
            @Valid @RequestBody NotificationPolicyCreateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(notificationPolicyService.createPolicy(req, currentUserId));
    }

    @PatchMapping("/{policyId}")
    public ApiResponse<NotificationPolicyResponse> updatePolicy(
            @PathVariable Long policyId,
            @Valid @RequestBody NotificationPolicyUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(notificationPolicyService.updatePolicy(policyId, req, currentUserId));
    }

    @PatchMapping("/{policyId}/status")
    public ApiResponse<Void> changePolicyStatus(
            @PathVariable Long policyId,
            @RequestBody Map<String, String> body) {
        notificationPolicyService.changePolicyStatus(policyId, body.get("isActive"));
        return ApiResponse.success();
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
