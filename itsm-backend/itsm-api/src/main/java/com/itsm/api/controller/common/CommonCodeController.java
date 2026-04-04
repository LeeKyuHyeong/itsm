package com.itsm.api.controller.common;

import com.itsm.api.dto.common.*;
import com.itsm.api.service.common.CommonCodeService;
import com.itsm.api.util.AuthUtils;
import com.itsm.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(commonCodeService.createGroup(req, currentUserId));
    }

    @PatchMapping("/api/v1/admin/common-codes/{groupId}")
    public ApiResponse<CommonCodeGroupResponse> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody CommonCodeGroupUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(commonCodeService.updateGroup(groupId, req, currentUserId));
    }

    @PatchMapping("/api/v1/admin/common-codes/{groupId}/status")
    public ApiResponse<Void> changeGroupStatus(
            @PathVariable Long groupId,
            @Valid @RequestBody IsActiveChangeRequest request) {
        commonCodeService.changeGroupStatus(groupId, request.getIsActive());
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
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(commonCodeService.createDetail(groupId, req, currentUserId));
    }

    @PatchMapping("/api/v1/admin/common-codes/{groupId}/details/{detailId}")
    public ApiResponse<CommonCodeDetailResponse> updateDetail(
            @PathVariable Long groupId,
            @PathVariable Long detailId,
            @Valid @RequestBody CommonCodeDetailUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(commonCodeService.updateDetail(groupId, detailId, req, currentUserId));
    }

    @PatchMapping("/api/v1/admin/common-codes/{groupId}/details/{detailId}/status")
    public ApiResponse<Void> changeDetailStatus(
            @PathVariable Long groupId,
            @PathVariable Long detailId,
            @Valid @RequestBody IsActiveChangeRequest request) {
        commonCodeService.changeDetailStatus(detailId, request.getIsActive());
        return ApiResponse.success(null);
    }

    @GetMapping("/api/v1/common-codes/{groupCd}")
    public ApiResponse<List<CommonCodeDetailResponse>> getActiveDetailsByGroupCd(
            @PathVariable String groupCd) {
        return ApiResponse.success(commonCodeService.getActiveDetailsByGroupCd(groupCd));
    }
}
