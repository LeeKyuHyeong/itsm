package com.itsm.api.controller.servicerequest;

import com.itsm.api.dto.common.ContentRequest;
import com.itsm.api.dto.common.StatusChangeRequest;
import com.itsm.api.dto.common.UserIdRequest;
import com.itsm.api.dto.servicerequest.*;
import com.itsm.api.service.servicerequest.ServiceRequestService;
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
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(serviceRequestService.create(req, currentUserId));
    }

    @PatchMapping("/{requestId}")
    public ApiResponse<SrResponse> update(
            @PathVariable Long requestId,
            @Valid @RequestBody SrUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(serviceRequestService.update(requestId, req, currentUserId));
    }

    @PatchMapping("/{requestId}/status")
    public ApiResponse<Void> changeStatus(
            @PathVariable Long requestId,
            @Valid @RequestBody StatusChangeRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        serviceRequestService.changeStatus(requestId, req.getStatus(), currentUserId);
        return ApiResponse.success();
    }

    @PostMapping("/{requestId}/assignees")
    public ApiResponse<SrAssigneeResponse> assignUser(
            @PathVariable Long requestId,
            @Valid @RequestBody UserIdRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(serviceRequestService.assignUser(requestId, req.getUserId(), currentUserId));
    }

    @DeleteMapping("/{requestId}/assignees/{userId}")
    public ApiResponse<Void> removeAssignee(
            @PathVariable Long requestId,
            @PathVariable Long userId,
            Authentication authentication) {
        AuthUtils.getCurrentUserId(authentication);
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
            @Valid @RequestBody SrProcessRequest req,
            Authentication authentication) {
        return ApiResponse.success(serviceRequestService.addProcess(requestId, req.getUserId(), req.getProcessContent()));
    }

    @PatchMapping("/{requestId}/processes/{processId}/complete")
    public ApiResponse<Void> completeProcess(
            @PathVariable Long requestId,
            @PathVariable Long processId,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        serviceRequestService.completeProcess(requestId, processId);
        serviceRequestService.checkAutoTransition(requestId, currentUserId);
        return ApiResponse.success();
    }

    @GetMapping("/{requestId}/processes")
    public ApiResponse<List<SrProcessResponse>> getProcesses(@PathVariable Long requestId) {
        return ApiResponse.success(serviceRequestService.getProcesses(requestId));
    }

    @PatchMapping("/{requestId}/schedule")
    public ApiResponse<SrResponse> setSchedule(
            @PathVariable Long requestId,
            @Valid @RequestBody SrScheduleRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(serviceRequestService.setSchedule(
                requestId, req.getScheduledAt(), req.getReason(), currentUserId));
    }

    @PostMapping("/{requestId}/satisfaction")
    public ApiResponse<Void> submitSatisfaction(
            @PathVariable Long requestId,
            @Valid @RequestBody SrSatisfactionRequest req,
            Authentication authentication) {
        serviceRequestService.submitSatisfaction(requestId, req.getScore(), req.getComment());
        return ApiResponse.success();
    }

    @GetMapping("/{requestId}/history")
    public ApiResponse<List<SrHistoryResponse>> getHistory(@PathVariable Long requestId) {
        return ApiResponse.success(serviceRequestService.getHistory(requestId));
    }
}
