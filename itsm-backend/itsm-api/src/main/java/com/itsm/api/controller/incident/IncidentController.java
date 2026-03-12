package com.itsm.api.controller.incident;

import com.itsm.api.dto.incident.*;
import com.itsm.api.service.incident.IncidentService;
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
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    public ApiResponse<Page<IncidentResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String statusCd,
            @RequestParam(required = false) String priorityCd,
            @RequestParam(required = false) String incidentTypeCd,
            Pageable pageable) {
        return ApiResponse.success(incidentService.search(keyword, companyId, statusCd, priorityCd, incidentTypeCd, pageable));
    }

    @GetMapping("/{incidentId}")
    public ApiResponse<IncidentResponse> getDetail(@PathVariable Long incidentId) {
        return ApiResponse.success(incidentService.getDetail(incidentId));
    }

    @PostMapping
    public ApiResponse<IncidentResponse> create(
            @Valid @RequestBody IncidentCreateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(incidentService.create(req, currentUserId));
    }

    @PatchMapping("/{incidentId}")
    public ApiResponse<IncidentResponse> update(
            @PathVariable Long incidentId,
            @Valid @RequestBody IncidentUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(incidentService.update(incidentId, req, currentUserId));
    }

    @PatchMapping("/{incidentId}/status")
    public ApiResponse<Void> changeStatus(
            @PathVariable Long incidentId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        incidentService.changeStatus(incidentId, body.get("status"), currentUserId);
        return ApiResponse.success();
    }

    @PostMapping("/{incidentId}/assignees")
    public ApiResponse<IncidentAssigneeResponse> assignUser(
            @PathVariable Long incidentId,
            @RequestBody Map<String, Long> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(incidentService.assignUser(incidentId, body.get("userId"), currentUserId));
    }

    @DeleteMapping("/{incidentId}/assignees/{userId}")
    public ApiResponse<Void> removeAssignee(
            @PathVariable Long incidentId,
            @PathVariable Long userId) {
        incidentService.removeAssignee(incidentId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/{incidentId}/assignees")
    public ApiResponse<List<IncidentAssigneeResponse>> getAssignees(@PathVariable Long incidentId) {
        return ApiResponse.success(incidentService.getAssignees(incidentId));
    }

    @GetMapping("/{incidentId}/comments")
    public ApiResponse<List<IncidentCommentResponse>> getComments(@PathVariable Long incidentId) {
        return ApiResponse.success(incidentService.getComments(incidentId));
    }

    @PostMapping("/{incidentId}/comments")
    public ApiResponse<IncidentCommentResponse> addComment(
            @PathVariable Long incidentId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(incidentService.addComment(incidentId, body.get("content"), currentUserId));
    }

    @PatchMapping("/{incidentId}/comments/{commentId}")
    public ApiResponse<IncidentCommentResponse> updateComment(
            @PathVariable Long incidentId,
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(incidentService.updateComment(incidentId, commentId, body.get("content"), currentUserId));
    }

    @DeleteMapping("/{incidentId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long incidentId,
            @PathVariable Long commentId) {
        incidentService.deleteComment(incidentId, commentId);
        return ApiResponse.success();
    }

    @GetMapping("/{incidentId}/history")
    public ApiResponse<List<IncidentHistoryResponse>> getHistory(@PathVariable Long incidentId) {
        return ApiResponse.success(incidentService.getHistory(incidentId));
    }

    @GetMapping("/{incidentId}/report")
    public ApiResponse<IncidentReportResponse> getReport(@PathVariable Long incidentId) {
        return ApiResponse.success(incidentService.getReport(incidentId));
    }

    @PostMapping("/{incidentId}/report")
    public ApiResponse<IncidentReportResponse> saveReport(
            @PathVariable Long incidentId,
            @Valid @RequestBody IncidentReportRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(incidentService.saveReport(incidentId, req, currentUserId));
    }

    @PatchMapping("/{incidentId}/report")
    public ApiResponse<IncidentReportResponse> updateReport(
            @PathVariable Long incidentId,
            @Valid @RequestBody IncidentReportRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(incidentService.updateReport(incidentId, req, currentUserId));
    }

    @PostMapping("/{incidentId}/assets")
    public ApiResponse<IncidentAssetResponse> addAsset(
            @PathVariable Long incidentId,
            @Valid @RequestBody IncidentAssetRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(incidentService.addAsset(incidentId, req, currentUserId));
    }

    @DeleteMapping("/{incidentId}/assets/{assetType}/{assetId}")
    public ApiResponse<Void> removeAsset(
            @PathVariable Long incidentId,
            @PathVariable String assetType,
            @PathVariable Long assetId) {
        incidentService.removeAsset(incidentId, assetType, assetId);
        return ApiResponse.success();
    }

    @GetMapping("/{incidentId}/assets")
    public ApiResponse<List<IncidentAssetResponse>> getAssets(@PathVariable Long incidentId) {
        return ApiResponse.success(incidentService.getAssets(incidentId));
    }

    @PatchMapping("/{incidentId}/main-manager")
    public ApiResponse<Void> assignMainManager(
            @PathVariable Long incidentId,
            @RequestBody Map<String, Long> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        incidentService.assignMainManager(incidentId, body.get("managerId"), currentUserId);
        return ApiResponse.success();
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
