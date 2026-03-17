package com.itsm.api.controller.asset;

import com.itsm.api.dto.asset.*;
import com.itsm.api.dto.common.StatusChangeRequest;
import com.itsm.api.service.asset.AssetOaService;
import com.itsm.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets/oa")
@RequiredArgsConstructor
public class AssetOaController {

    private final AssetOaService assetOaService;

    @GetMapping
    public ApiResponse<Page<AssetOaResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String assetTypeCd,
            Pageable pageable) {
        return ApiResponse.success(assetOaService.search(keyword, companyId, status, assetTypeCd, pageable));
    }

    @GetMapping("/{assetOaId}")
    public ApiResponse<AssetOaResponse> getDetail(@PathVariable Long assetOaId) {
        return ApiResponse.success(assetOaService.getDetail(assetOaId));
    }

    @PostMapping
    public ApiResponse<AssetOaResponse> create(
            @Valid @RequestBody AssetOaCreateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(assetOaService.create(req, currentUserId));
    }

    @PatchMapping("/{assetOaId}")
    public ApiResponse<AssetOaResponse> update(
            @PathVariable Long assetOaId,
            @Valid @RequestBody AssetOaUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(assetOaService.update(assetOaId, req, currentUserId));
    }

    @PatchMapping("/{assetOaId}/status")
    public ApiResponse<Void> changeStatus(
            @PathVariable Long assetOaId,
            @Valid @RequestBody StatusChangeRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        assetOaService.changeStatus(assetOaId, req.getStatus(), currentUserId);
        return ApiResponse.success();
    }

    @GetMapping("/{assetOaId}/history")
    public ApiResponse<List<AssetHistoryResponse>> getHistory(@PathVariable Long assetOaId) {
        return ApiResponse.success(assetOaService.getHistory(assetOaId));
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
