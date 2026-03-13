package com.itsm.api.controller.asset;

import com.itsm.api.dto.asset.*;
import com.itsm.api.service.asset.AssetHwService;
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
@RequestMapping("/api/v1/assets/hw")
@RequiredArgsConstructor
public class AssetHwController {

    private final AssetHwService assetHwService;

    @GetMapping
    public ApiResponse<Page<AssetHwResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String assetTypeCd,
            @RequestParam(required = false) String assetCategory,
            @RequestParam(required = false) String assetSubCategory,
            Pageable pageable) {
        return ApiResponse.success(assetHwService.search(keyword, companyId, status, assetTypeCd,
                assetCategory, assetSubCategory, pageable));
    }

    @GetMapping("/{assetHwId}")
    public ApiResponse<AssetHwResponse> getDetail(@PathVariable Long assetHwId) {
        return ApiResponse.success(assetHwService.getDetail(assetHwId));
    }

    @PostMapping
    public ApiResponse<AssetHwResponse> create(
            @Valid @RequestBody AssetHwCreateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(assetHwService.create(req, currentUserId));
    }

    @PatchMapping("/{assetHwId}")
    public ApiResponse<AssetHwResponse> update(
            @PathVariable Long assetHwId,
            @Valid @RequestBody AssetHwUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(assetHwService.update(assetHwId, req, currentUserId));
    }

    @PatchMapping("/{assetHwId}/status")
    public ApiResponse<Void> changeStatus(
            @PathVariable Long assetHwId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        assetHwService.changeStatus(assetHwId, body.get("status"), currentUserId);
        return ApiResponse.success();
    }

    @GetMapping("/{assetHwId}/history")
    public ApiResponse<List<AssetHistoryResponse>> getHistory(@PathVariable Long assetHwId) {
        return ApiResponse.success(assetHwService.getHistory(assetHwId));
    }

    @PostMapping("/relations")
    public ApiResponse<AssetRelationResponse> addRelation(
            @Valid @RequestBody AssetRelationRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(assetHwService.addRelation(req, currentUserId));
    }

    @DeleteMapping("/{assetHwId}/relations/{assetSwId}")
    public ApiResponse<Void> removeRelation(
            @PathVariable Long assetHwId,
            @PathVariable Long assetSwId) {
        assetHwService.removeRelation(assetHwId, assetSwId);
        return ApiResponse.success();
    }

    @GetMapping("/{assetHwId}/relations")
    public ApiResponse<List<AssetRelationResponse>> getRelations(@PathVariable Long assetHwId) {
        return ApiResponse.success(assetHwService.getRelations(assetHwId));
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
