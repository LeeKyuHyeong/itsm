package com.itsm.api.controller.common;

import com.itsm.api.dto.common.SlaPolicyCreateRequest;
import com.itsm.api.dto.common.SlaPolicyResponse;
import com.itsm.api.dto.common.SlaPolicyUpdateRequest;
import com.itsm.api.service.common.SlaPolicyService;
import com.itsm.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/sla-policies")
@RequiredArgsConstructor
public class SlaPolicyController {

    private final SlaPolicyService slaPolicyService;

    @GetMapping
    public ApiResponse<List<SlaPolicyResponse>> getPolicies(
            @RequestParam(required = false) Long companyId) {
        if (companyId != null) {
            return ApiResponse.success(slaPolicyService.getPoliciesByCompany(companyId));
        }
        return ApiResponse.success(slaPolicyService.getAllPolicies());
    }

    @PostMapping
    public ApiResponse<SlaPolicyResponse> createPolicy(
            @Valid @RequestBody SlaPolicyCreateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(slaPolicyService.createPolicy(req, currentUserId));
    }

    @PatchMapping("/{policyId}")
    public ApiResponse<SlaPolicyResponse> updatePolicy(
            @PathVariable Long policyId,
            @Valid @RequestBody SlaPolicyUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(slaPolicyService.updatePolicy(policyId, req, currentUserId));
    }

    @PatchMapping("/{policyId}/status")
    public ApiResponse<Void> changePolicyStatus(
            @PathVariable Long policyId,
            @RequestBody Map<String, String> body) {
        slaPolicyService.changePolicyStatus(policyId, body.get("isActive"));
        return ApiResponse.success();
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
