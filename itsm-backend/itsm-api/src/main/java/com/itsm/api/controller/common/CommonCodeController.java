package com.itsm.api.controller.common;

import com.itsm.api.dto.common.*;
import com.itsm.api.service.common.CommonCodeService;
import com.itsm.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    @GetMapping("/api/v1/admin/common-codes")
    public ApiResponse<List<CommonCodeGroupResponse>> getGroups() {
        return ApiResponse.success(commonCodeService.getGroups());
    }

    @PostMapping("/api/v1/admin/common-codes")
    public ApiResponse<CommonCodeGroupResponse> createGroup(
            @Valid @RequestBody CommonCodeGroupCreateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(commonCodeService.createGroup(req, currentUserId));
    }

    @PatchMapping("/api/v1/admin/common-codes/{groupId}")
    public ApiResponse<CommonCodeGroupResponse> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody CommonCodeGroupUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(commonCodeService.updateGroup(groupId, req, currentUserId));
    }

    @PatchMapping("/api/v1/admin/common-codes/{groupId}/status")
    public ApiResponse<Void> changeGroupStatus(
            @PathVariable Long groupId,
            @RequestBody Map<String, String> request) {
        commonCodeService.changeGroupStatus(groupId, request.get("isActive"));
        return ApiResponse.success(null);
    }

    @GetMapping("/api/v1/admin/common-codes/{groupId}/details")
    public ApiResponse<List<CommonCodeDetailResponse>> getDetails(@PathVariable Long groupId) {
        return ApiResponse.success(commonCodeService.getDetails(groupId));
    }

    @PostMapping("/api/v1/admin/common-codes/{groupId}/details")
    public ApiResponse<CommonCodeDetailResponse> createDetail(
            @PathVariable Long groupId,
            @Valid @RequestBody CommonCodeDetailCreateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(commonCodeService.createDetail(groupId, req, currentUserId));
    }

    @PatchMapping("/api/v1/admin/common-codes/{groupId}/details/{detailId}")
    public ApiResponse<CommonCodeDetailResponse> updateDetail(
            @PathVariable Long groupId,
            @PathVariable Long detailId,
            @Valid @RequestBody CommonCodeDetailUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(commonCodeService.updateDetail(groupId, detailId, req, currentUserId));
    }

    @PatchMapping("/api/v1/admin/common-codes/{groupId}/details/{detailId}/status")
    public ApiResponse<Void> changeDetailStatus(
            @PathVariable Long groupId,
            @PathVariable Long detailId,
            @RequestBody Map<String, String> request) {
        commonCodeService.changeDetailStatus(detailId, request.get("isActive"));
        return ApiResponse.success(null);
    }

    @GetMapping("/api/v1/common-codes/{groupCd}")
    public ApiResponse<List<CommonCodeDetailResponse>> getActiveDetailsByGroupCd(
            @PathVariable String groupCd) {
        return ApiResponse.success(commonCodeService.getActiveDetailsByGroupCd(groupCd));
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
