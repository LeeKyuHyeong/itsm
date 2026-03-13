package com.itsm.api.controller.asset;

import com.itsm.api.dto.asset.*;
import com.itsm.api.service.asset.AssetSwService;
import com.itsm.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(assetSwService.create(req, currentUserId));
    }

    @PatchMapping("/{assetSwId}")
    public ApiResponse<AssetSwResponse> update(
            @PathVariable Long assetSwId,
            @Valid @RequestBody AssetSwUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(assetSwService.update(assetSwId, req, currentUserId));
    }

    @PatchMapping("/{assetSwId}/status")
    public ApiResponse<Void> changeStatus(
            @PathVariable Long assetSwId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        assetSwService.changeStatus(assetSwId, body.get("status"), currentUserId);
        return ApiResponse.success();
    }

    @GetMapping("/{assetSwId}/history")
    public ApiResponse<List<AssetHistoryResponse>> getHistory(@PathVariable Long assetSwId) {
        return ApiResponse.success(assetSwService.getHistory(assetSwId));
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
