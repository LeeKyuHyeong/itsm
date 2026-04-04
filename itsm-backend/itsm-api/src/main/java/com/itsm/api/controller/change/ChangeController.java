package com.itsm.api.controller.change;

import com.itsm.api.dto.change.*;
import com.itsm.api.dto.common.ContentRequest;
import com.itsm.api.dto.common.StatusChangeRequest;
import com.itsm.api.dto.common.UserIdRequest;
import com.itsm.api.service.change.ChangeService;
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
@RequestMapping("/api/v1/changes")
@RequiredArgsConstructor
public class ChangeController {

    private final ChangeService changeService;

    @GetMapping
    public ApiResponse<Page<ChangeResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String statusCd,
            @RequestParam(required = false) String priorityCd,
            @RequestParam(required = false) String changeTypeCd,
            Pageable pageable) {
        return ApiResponse.success(changeService.search(keyword, companyId, statusCd, priorityCd, changeTypeCd, pageable));
    }

    @GetMapping("/{changeId}")
    public ApiResponse<ChangeResponse> getDetail(@PathVariable Long changeId) {
        return ApiResponse.success(changeService.getDetail(changeId));
    }

    @PostMapping
    public ApiResponse<ChangeResponse> create(
            @Valid @RequestBody ChangeCreateRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(changeService.create(req, currentUserId));
    }

    @PatchMapping("/{changeId}")
    public ApiResponse<ChangeResponse> update(
            @PathVariable Long changeId,
            @Valid @RequestBody ChangeUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(changeService.update(changeId, req, currentUserId));
    }

    @PatchMapping("/{changeId}/status")
    public ApiResponse<Void> changeStatus(
            @PathVariable Long changeId,
            @Valid @RequestBody StatusChangeRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        changeService.changeStatus(changeId, req.getStatus(), currentUserId);
        return ApiResponse.success();
    }

    @PostMapping("/{changeId}/approvers")
    public ApiResponse<ChangeApproverResponse> addApprover(
            @PathVariable Long changeId,
            @Valid @RequestBody UserIdRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(changeService.addApprover(changeId, req.getUserId(), currentUserId));
    }

    @DeleteMapping("/{changeId}/approvers/{userId}")
    public ApiResponse<Void> removeApprover(
            @PathVariable Long changeId,
            @PathVariable Long userId,
            Authentication authentication) {
        AuthUtils.getCurrentUserId(authentication);
        changeService.removeApprover(changeId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/{changeId}/approvers")
    public ApiResponse<List<ChangeApproverResponse>> getApprovers(@PathVariable Long changeId) {
        return ApiResponse.success(changeService.getApprovers(changeId));
    }

    @PatchMapping("/{changeId}/approvers/{userId}")
    public ApiResponse<Void> approveChange(
            @PathVariable Long changeId,
            @PathVariable Long userId,
            @Valid @RequestBody ApprovalDecisionRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        changeService.approveChange(changeId, userId, req.getDecision(), req.getComment(), currentUserId);
        return ApiResponse.success();
    }

    @GetMapping("/{changeId}/comments")
    public ApiResponse<List<ChangeCommentResponse>> getComments(@PathVariable Long changeId) {
        return ApiResponse.success(changeService.getComments(changeId));
    }

    @PostMapping("/{changeId}/comments")
    public ApiResponse<ChangeCommentResponse> addComment(
            @PathVariable Long changeId,
            @Valid @RequestBody ContentRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(changeService.addComment(changeId, req.getContent(), currentUserId));
    }

    @PatchMapping("/{changeId}/comments/{commentId}")
    public ApiResponse<ChangeCommentResponse> updateComment(
            @PathVariable Long changeId,
            @PathVariable Long commentId,
            @Valid @RequestBody ContentRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(changeService.updateComment(changeId, commentId, req.getContent(), currentUserId));
    }

    @DeleteMapping("/{changeId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long changeId,
            @PathVariable Long commentId,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        changeService.deleteComment(changeId, commentId, currentUserId);
        return ApiResponse.success();
    }

    @GetMapping("/{changeId}/history")
    public ApiResponse<List<ChangeHistoryResponse>> getHistory(@PathVariable Long changeId) {
        return ApiResponse.success(changeService.getHistory(changeId));
    }
}
