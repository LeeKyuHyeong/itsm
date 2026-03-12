package com.itsm.api.controller.change;

import com.itsm.api.dto.change.*;
import com.itsm.api.service.change.ChangeService;
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
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(changeService.create(req, currentUserId));
    }

    @PatchMapping("/{changeId}")
    public ApiResponse<ChangeResponse> update(
            @PathVariable Long changeId,
            @Valid @RequestBody ChangeUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(changeService.update(changeId, req, currentUserId));
    }

    @PatchMapping("/{changeId}/status")
    public ApiResponse<Void> changeStatus(
            @PathVariable Long changeId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        changeService.changeStatus(changeId, body.get("status"), currentUserId);
        return ApiResponse.success();
    }

    @PostMapping("/{changeId}/approvers")
    public ApiResponse<ChangeApproverResponse> addApprover(
            @PathVariable Long changeId,
            @RequestBody Map<String, Long> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(changeService.addApprover(changeId, body.get("userId"), currentUserId));
    }

    @DeleteMapping("/{changeId}/approvers/{userId}")
    public ApiResponse<Void> removeApprover(
            @PathVariable Long changeId,
            @PathVariable Long userId) {
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
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        changeService.approveChange(changeId, userId, body.get("decision"), body.get("comment"), currentUserId);
        return ApiResponse.success();
    }

    @GetMapping("/{changeId}/comments")
    public ApiResponse<List<ChangeCommentResponse>> getComments(@PathVariable Long changeId) {
        return ApiResponse.success(changeService.getComments(changeId));
    }

    @PostMapping("/{changeId}/comments")
    public ApiResponse<ChangeCommentResponse> addComment(
            @PathVariable Long changeId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(changeService.addComment(changeId, body.get("content"), currentUserId));
    }

    @PatchMapping("/{changeId}/comments/{commentId}")
    public ApiResponse<ChangeCommentResponse> updateComment(
            @PathVariable Long changeId,
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(changeService.updateComment(changeId, commentId, body.get("content"), currentUserId));
    }

    @DeleteMapping("/{changeId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long changeId,
            @PathVariable Long commentId) {
        changeService.deleteComment(changeId, commentId);
        return ApiResponse.success();
    }

    @GetMapping("/{changeId}/history")
    public ApiResponse<List<ChangeHistoryResponse>> getHistory(@PathVariable Long changeId) {
        return ApiResponse.success(changeService.getHistory(changeId));
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
