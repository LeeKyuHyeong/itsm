package com.itsm.api.controller.board;

import com.itsm.api.dto.board.*;
import com.itsm.api.dto.common.ContentRequest;
import com.itsm.api.service.board.BoardService;
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
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // ===== Board Config (Admin) =====

    @GetMapping("/api/v1/admin/boards")
    public ApiResponse<List<BoardConfigResponse>> getConfigList() {
        return ApiResponse.success(boardService.getConfigList());
    }

    @PostMapping("/api/v1/admin/boards")
    public ApiResponse<BoardConfigResponse> createConfig(
            @Valid @RequestBody BoardConfigRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(boardService.createConfig(req, currentUserId));
    }

    @PutMapping("/api/v1/admin/boards/{boardId}")
    public ApiResponse<BoardConfigResponse> updateConfig(
            @PathVariable Long boardId,
            @Valid @RequestBody BoardConfigRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(boardService.updateConfig(boardId, req, currentUserId));
    }

    @DeleteMapping("/api/v1/admin/boards/{boardId}")
    public ApiResponse<Void> deleteConfig(@PathVariable Long boardId) {
        boardService.deleteConfig(boardId);
        return ApiResponse.success();
    }

    // ===== Posts =====

    @GetMapping("/api/v1/boards/{boardId}/posts")
    public ApiResponse<Page<BoardPostResponse>> getPostList(
            @PathVariable Long boardId,
            Pageable pageable) {
        return ApiResponse.success(boardService.getPostList(boardId, pageable));
    }

    @PostMapping("/api/v1/boards/{boardId}/posts")
    public ApiResponse<BoardPostResponse> createPost(
            @PathVariable Long boardId,
            @Valid @RequestBody BoardPostCreateRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(boardService.createPost(boardId, req, currentUserId));
    }

    @GetMapping("/api/v1/boards/{boardId}/posts/{postId}")
    public ApiResponse<BoardPostResponse> getPostDetail(
            @PathVariable Long boardId,
            @PathVariable Long postId) {
        return ApiResponse.success(boardService.getPostDetail(boardId, postId));
    }

    @PutMapping("/api/v1/boards/{boardId}/posts/{postId}")
    public ApiResponse<BoardPostResponse> updatePost(
            @PathVariable Long boardId,
            @PathVariable Long postId,
            @Valid @RequestBody BoardPostUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(boardService.updatePost(boardId, postId, req, currentUserId));
    }

    @DeleteMapping("/api/v1/boards/{boardId}/posts/{postId}")
    public ApiResponse<Void> deletePost(
            @PathVariable Long boardId,
            @PathVariable Long postId) {
        boardService.deletePost(boardId, postId);
        return ApiResponse.success();
    }

    // ===== Comments =====

    @GetMapping("/api/v1/boards/{boardId}/posts/{postId}/comments")
    public ApiResponse<List<BoardCommentResponse>> getCommentList(
            @PathVariable Long boardId,
            @PathVariable Long postId) {
        return ApiResponse.success(boardService.getCommentList(boardId, postId));
    }

    @PostMapping("/api/v1/boards/{boardId}/posts/{postId}/comments")
    public ApiResponse<BoardCommentResponse> createComment(
            @PathVariable Long boardId,
            @PathVariable Long postId,
            @Valid @RequestBody ContentRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(boardService.createComment(boardId, postId, req.getContent(), currentUserId));
    }

    @PutMapping("/api/v1/boards/{boardId}/posts/{postId}/comments/{commentId}")
    public ApiResponse<BoardCommentResponse> updateComment(
            @PathVariable Long boardId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody ContentRequest req,
            Authentication authentication) {
        Long currentUserId = AuthUtils.getCurrentUserId(authentication);
        return ApiResponse.success(boardService.updateComment(boardId, postId, commentId, req.getContent(), currentUserId));
    }

    @DeleteMapping("/api/v1/boards/{boardId}/posts/{postId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        boardService.deleteComment(boardId, postId, commentId);
        return ApiResponse.success();
    }
}
