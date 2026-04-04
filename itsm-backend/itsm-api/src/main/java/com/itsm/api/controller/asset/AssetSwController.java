package com.itsm.api.controller.asset;

import com.itsm.api.dto.asset.*;
import com.itsm.api.dto.common.StatusChangeRequest;
import com.itsm.api.service.asset.AssetSwService;
import com.itsm.api.util.AuthUtils;
import com.itsm.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets/sw")
@RequiredArgsConstructor
public class AssetSwController {

    private final AssetSwService assetSwService;

    @GetMapping
    public ApiResponse<Page<AssetSwResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String swTypeCd,
            @RequestParam(required = false) String assetCategory,
            @RequestParam(required = false) String assetSubCategory,
            Pageable pageable) {
        return ApiResponse.success(assetSwService.search(keyword, companyId, status, swTypeCd,
                assetCategory, assetSubCategory, pageable));
    }

    @GetMapping("/{assetSwId}")
    public ApiResponse<AssetSwResponse> getDetail(@PathVariable Long assetSwId) {
        return ApiResponse.success(assetSwService.getDetail(assetSwId));
    }

    @PostMapping
    public ApiResponse<AssetSwResponse> create(
            @Valid @RequestBody AssetSwCreateRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(assetSwService.create(req, currentUserId));
    }

    @PatchMapping("/{assetSwId}")
    public ApiResponse<AssetSwResponse> update(
            @PathVariable Long assetSwId,
            @Valid @RequestBody AssetSwUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(assetSwService.update(assetSwId, req, currentUserId));
    }

    @PatchMapping("/{assetSwId}/status")
    public ApiResponse<Void> changeStatus(
            @PathVariable Long assetSwId,
            @Valid @RequestBody StatusChangeRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        assetSwService.changeStatus(assetSwId, req.getStatus(), currentUserId);
        return ApiResponse.success();
    }

    @GetMapping("/{assetSwId}/history")
    public ApiResponse<List<AssetHistoryResponse>> getHistory(@PathVariable Long assetSwId) {
        return ApiResponse.success(assetSwService.getHistory(assetSwId));
    }
}
