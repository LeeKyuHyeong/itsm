package com.itsm.api.service.board;

import com.itsm.api.dto.board.*;
import com.itsm.core.domain.board.*;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.board.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

    private final BoardConfigRepository boardConfigRepository;
    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final BoardFileRepository boardFileRepository;

    // ===== Board Config CRUD =====

    @Transactional(readOnly = true)
    public List<BoardConfigResponse> getConfigList() {
        return boardConfigRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toConfigResponse)
                .toList();
    }

    public BoardConfigResponse createConfig(BoardConfigRequest req, Long currentUserId) {
        BoardConfig config = BoardConfig.builder()
                .boardNm(req.getBoardNm())
                .boardTypeCd(req.getBoardTypeCd())
                .allowExt(req.getAllowExt())
                .maxFileSize(req.getMaxFileSize())
                .allowComment(req.getAllowComment())
                .rolePermission(req.getRolePermission())
                .isActive(req.getIsActive())
                .sortOrder(req.getSortOrder())
                .build();
        config.setCreatedBy(currentUserId);

        BoardConfig saved = boardConfigRepository.save(config);
        return toConfigResponse(saved);
    }

    public BoardConfigResponse updateConfig(Long boardId, BoardConfigRequest req, Long currentUserId) {
        BoardConfig config = findConfigById(boardId);
        config.update(req.getBoardNm(), req.getBoardTypeCd(), req.getAllowExt(),
                req.getMaxFileSize(), req.getAllowComment(), req.getRolePermission(),
                req.getIsActive(), req.getSortOrder());
        config.setUpdatedBy(currentUserId);
        return toConfigResponse(config);
    }

    public void deleteConfig(Long boardId) {
        BoardConfig config = findConfigById(boardId);
        boardConfigRepository.delete(config);
    }

    // ===== Post CRUD =====

    @Transactional(readOnly = true)
    public Page<BoardPostResponse> getPostList(Long boardId, Pageable pageable) {
        findConfigById(boardId);
        return boardPostRepository.findByBoardConfig_BoardIdOrderByIsNoticeDescCreatedAtDesc(boardId, pageable)
                .map(this::toPostResponse);
    }

    public BoardPostResponse createPost(Long boardId, BoardPostCreateRequest req, Long currentUserId) {
        BoardConfig config = findConfigById(boardId);

        BoardPost post = BoardPost.builder()
                .boardConfig(config)
                .title(req.getTitle())
                .content(req.getContent())
                .isNotice(req.getIsNotice())
                .build();
        post.setCreatedBy(currentUserId);

        BoardPost saved = boardPostRepository.save(post);
        return toPostResponse(saved);
    }

    public BoardPostResponse getPostDetail(Long boardId, Long postId) {
        BoardPost post = findPostById(postId);
        post.incrementViewCnt();
        return toPostResponse(post);
    }

    public BoardPostResponse updatePost(Long boardId, Long postId, BoardPostUpdateRequest req, Long currentUserId) {
        BoardPost post = findPostById(postId);
        post.update(req.getTitle(), req.getContent(), req.getIsNotice());
        post.setUpdatedBy(currentUserId);
        return toPostResponse(post);
    }

    public void deletePost(Long boardId, Long postId) {
        BoardPost post = findPostById(postId);
        boardPostRepository.delete(post);
    }

    // ===== Comment CRUD =====

    @Transactional(readOnly = true)
    public List<BoardCommentResponse> getCommentList(Long boardId, Long postId) {
        return boardCommentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(this::toCommentResponse)
                .toList();
    }

    public BoardCommentResponse createComment(Long boardId, Long postId, String content, Long currentUserId) {
        findPostById(postId);
        BoardComment comment = BoardComment.builder()
                .postId(postId)
                .content(content)
                .createdBy(currentUserId)
                .build();
        BoardComment saved = boardCommentRepository.save(comment);
        return toCommentResponse(saved);
    }

    public BoardCommentResponse updateComment(Long boardId, Long postId, Long commentId,
                                               String content, Long currentUserId) {
        BoardComment comment = findCommentById(commentId);
        comment.updateContent(content, currentUserId);
        return toCommentResponse(comment);
    }

    public void deleteComment(Long boardId, Long postId, Long commentId) {
        BoardComment comment = findCommentById(commentId);
        boardCommentRepository.delete(comment);
    }

    // ===== Private helpers =====

    private BoardConfig findConfigById(Long boardId) {
        return boardConfigRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "게시판을 찾을 수 없습니다."));
    }

    private BoardPost findPostById(Long postId) {
        return boardPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "게시글을 찾을 수 없습니다."));
    }

    private BoardComment findCommentById(Long commentId) {
        return boardCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "댓글을 찾을 수 없습니다."));
    }

    private BoardConfigResponse toConfigResponse(BoardConfig config) {
        return BoardConfigResponse.builder()
                .boardId(config.getBoardId())
                .boardNm(config.getBoardNm())
                .boardTypeCd(config.getBoardTypeCd())
                .allowExt(config.getAllowExt())
                .maxFileSize(config.getMaxFileSize())
                .allowComment(config.getAllowComment())
                .rolePermission(config.getRolePermission())
                .isActive(config.getIsActive())
                .sortOrder(config.getSortOrder())
                .createdAt(config.getCreatedAt())
                .build();
    }

    private BoardPostResponse toPostResponse(BoardPost post) {
        return BoardPostResponse.builder()
                .postId(post.getPostId())
                .boardId(post.getBoardConfig() != null ? post.getBoardConfig().getBoardId() : null)
                .boardNm(post.getBoardConfig() != null ? post.getBoardConfig().getBoardNm() : null)
                .title(post.getTitle())
                .content(post.getContent())
                .viewCnt(post.getViewCnt())
                .isNotice(post.getIsNotice())
                .createdBy(post.getCreatedBy())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private BoardCommentResponse toCommentResponse(BoardComment comment) {
        return BoardCommentResponse.builder()
                .commentId(comment.getCommentId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .createdBy(comment.getCreatedBy())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
