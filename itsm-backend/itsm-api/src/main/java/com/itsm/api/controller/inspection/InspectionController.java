package com.itsm.api.controller.inspection;

import com.itsm.api.dto.inspection.*;
import com.itsm.api.service.inspection.InspectionService;
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
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    @GetMapping
    public ApiResponse<Page<InspectionResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String statusCd,
            @RequestParam(required = false) String inspectionTypeCd,
            Pageable pageable) {
        return ApiResponse.success(inspectionService.search(keyword, companyId, statusCd, inspectionTypeCd, pageable));
    }

    @GetMapping("/{inspectionId}")
    public ApiResponse<InspectionResponse> getDetail(@PathVariable Long inspectionId) {
        return ApiResponse.success(inspectionService.getDetail(inspectionId));
    }

    @PostMapping
    public ApiResponse<InspectionResponse> create(
            @Valid @RequestBody InspectionCreateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(inspectionService.create(req, currentUserId));
    }

    @PatchMapping("/{inspectionId}")
    public ApiResponse<InspectionResponse> update(
            @PathVariable Long inspectionId,
            @Valid @RequestBody InspectionUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(inspectionService.update(inspectionId, req, currentUserId));
    }

    @PatchMapping("/{inspectionId}/status")
    public ApiResponse<Void> changeStatus(
            @PathVariable Long inspectionId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        inspectionService.changeStatus(inspectionId, body.get("status"), currentUserId);
        return ApiResponse.success();
    }

    @GetMapping("/{inspectionId}/items")
    public ApiResponse<List<InspectionItemResponse>> getItems(@PathVariable Long inspectionId) {
        return ApiResponse.success(inspectionService.getItems(inspectionId));
    }

    @PostMapping("/{inspectionId}/items")
    public ApiResponse<InspectionItemResponse> addItem(
            @PathVariable Long inspectionId,
            @Valid @RequestBody InspectionItemRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(inspectionService.addItem(inspectionId, req, currentUserId));
    }

    @DeleteMapping("/{inspectionId}/items/{itemId}")
    public ApiResponse<Void> deleteItem(
            @PathVariable Long inspectionId,
            @PathVariable Long itemId) {
        inspectionService.deleteItem(inspectionId, itemId);
        return ApiResponse.success();
    }

    @PostMapping("/{inspectionId}/results")
    public ApiResponse<InspectionResultResponse> addResult(
            @PathVariable Long inspectionId,
            @Valid @RequestBody InspectionResultRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(inspectionService.addResult(inspectionId, req, currentUserId));
    }

    @GetMapping("/{inspectionId}/results")
    public ApiResponse<List<InspectionResultResponse>> getResults(@PathVariable Long inspectionId) {
        return ApiResponse.success(inspectionService.getResults(inspectionId));
    }

    @GetMapping("/{inspectionId}/history")
    public ApiResponse<List<InspectionHistoryResponse>> getHistory(@PathVariable Long inspectionId) {
        return ApiResponse.success(inspectionService.getHistory(inspectionId));
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
