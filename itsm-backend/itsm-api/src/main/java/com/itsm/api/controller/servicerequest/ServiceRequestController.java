package com.itsm.api.controller.servicerequest;

import com.itsm.api.dto.servicerequest.*;
import com.itsm.api.service.servicerequest.ServiceRequestService;
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
@RequestMapping("/api/v1/service-requests")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @GetMapping
    public ApiResponse<Page<SrResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String statusCd,
            @RequestParam(required = false) String priorityCd,
            @RequestParam(required = false) String requestTypeCd,
            Pageable pageable) {
        return ApiResponse.success(serviceRequestService.search(keyword, companyId, statusCd, priorityCd, requestTypeCd, pageable));
    }

    @GetMapping("/{requestId}")
    public ApiResponse<SrResponse> getDetail(@PathVariable Long requestId) {
        return ApiResponse.success(serviceRequestService.getDetail(requestId));
    }

    @PostMapping
    public ApiResponse<SrResponse> create(
            @Valid @RequestBody SrCreateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(serviceRequestService.create(req, currentUserId));
    }

    @PatchMapping("/{requestId}")
    public ApiResponse<SrResponse> update(
            @PathVariable Long requestId,
            @Valid @RequestBody SrUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(serviceRequestService.update(requestId, req, currentUserId));
    }

    @PatchMapping("/{requestId}/status")
    public ApiResponse<Void> changeStatus(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        serviceRequestService.changeStatus(requestId, body.get("status"), currentUserId);
        return ApiResponse.success();
    }

    @PostMapping("/{requestId}/assignees")
    public ApiResponse<SrAssigneeResponse> assignUser(
            @PathVariable Long requestId,
            @RequestBody Map<String, Long> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(serviceRequestService.assignUser(requestId, body.get("userId"), currentUserId));
    }

    @DeleteMapping("/{requestId}/assignees/{userId}")
    public ApiResponse<Void> removeAssignee(
            @PathVariable Long requestId,
            @PathVariable Long userId) {
        serviceRequestService.removeAssignee(requestId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/{requestId}/assignees")
    public ApiResponse<List<SrAssigneeResponse>> getAssignees(@PathVariable Long requestId) {
        return ApiResponse.success(serviceRequestService.getAssignees(requestId));
    }

    @PostMapping("/{requestId}/processes")
    public ApiResponse<SrProcessResponse> addProcess(
            @PathVariable Long requestId,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        Long userId = ((Number) body.get("userId")).longValue();
        String processContent = (String) body.get("processContent");
        return ApiResponse.success(serviceRequestService.addProcess(requestId, userId, processContent));
    }

    @PatchMapping("/{requestId}/processes/{processId}/complete")
    public ApiResponse<Void> completeProcess(
            @PathVariable Long requestId,
            @PathVariable Long processId,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        serviceRequestService.completeProcess(requestId, processId);
        serviceRequestService.checkAutoTransition(requestId, currentUserId);
        return ApiResponse.success();
    }

    @GetMapping("/{requestId}/processes")
    public ApiResponse<List<SrProcessResponse>> getProcesses(@PathVariable Long requestId) {
        return ApiResponse.success(serviceRequestService.getProcesses(requestId));
    }

    @PostMapping("/{requestId}/satisfaction")
    public ApiResponse<Void> submitSatisfaction(
            @PathVariable Long requestId,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        int score = ((Number) body.get("score")).intValue();
        String comment = (String) body.get("comment");
        serviceRequestService.submitSatisfaction(requestId, score, comment);
        return ApiResponse.success();
    }

    @GetMapping("/{requestId}/history")
    public ApiResponse<List<SrHistoryResponse>> getHistory(@PathVariable Long requestId) {
        return ApiResponse.success(serviceRequestService.getHistory(requestId));
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
