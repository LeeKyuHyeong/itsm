package com.itsm.api.service.board;

import com.itsm.api.dto.board.*;
import com.itsm.core.domain.board.*;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.board.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock private BoardConfigRepository boardConfigRepository;
    @Mock private BoardPostRepository boardPostRepository;
    @Mock private BoardCommentRepository boardCommentRepository;
    @Mock private BoardFileRepository boardFileRepository;

    @InjectMocks
    private BoardService boardService;

    private BoardConfig boardConfig;
    private BoardPost boardPost;

    @BeforeEach
    void setUp() {
        boardConfig = BoardConfig.builder()
                .boardNm("공지사항")
                .boardTypeCd("NOTICE")
                .allowExt("pdf,doc")
                .maxFileSize(10)
                .allowComment("Y")
                .rolePermission("{\"read\":[\"USER\"],\"write\":[\"ADMIN\"]}")
                .isActive("Y")
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(boardConfig, "boardId", 1L);

        boardPost = BoardPost.builder()
                .boardConfig(boardConfig)
                .title("공지 제목")
                .content("공지 내용")
                .isNotice("Y")
                .build();
        ReflectionTestUtils.setField(boardPost, "postId", 1L);
    }

    // ===== Board Config CRUD =====

    @Test
    @DisplayName("게시판 설정 목록을 조회한다")
    void getConfigList_returnsList() {
        given(boardConfigRepository.findAllByOrderBySortOrderAsc()).willReturn(List.of(boardConfig));

        List<BoardConfigResponse> result = boardService.getConfigList();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBoardNm()).isEqualTo("공지사항");
    }

    @Test
    @DisplayName("게시판 설정을 생성한다")
    void createConfig_success() {
        BoardConfigRequest req = new BoardConfigRequest(
                "자유게시판", "FREE", "jpg,png", 5, "Y", "{}", "Y", 2);

        given(boardConfigRepository.save(any(BoardConfig.class))).willAnswer(inv -> {
            BoardConfig saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "boardId", 2L);
            return saved;
        });

        BoardConfigResponse result = boardService.createConfig(req, 1L);

        assertThat(result.getBoardNm()).isEqualTo("자유게시판");
        assertThat(result.getBoardTypeCd()).isEqualTo("FREE");
        verify(boardConfigRepository).save(any(BoardConfig.class));
    }

    @Test
    @DisplayName("게시판 설정을 수정한다")
    void updateConfig_success() {
        BoardConfigRequest req = new BoardConfigRequest(
                "수정된 공지사항", "NOTICE", "pdf", 20, "N", "{}", "Y", 3);

        given(boardConfigRepository.findById(1L)).willReturn(Optional.of(boardConfig));

        BoardConfigResponse result = boardService.updateConfig(1L, req, 1L);

        assertThat(result.getBoardNm()).isEqualTo("수정된 공지사항");
        assertThat(result.getMaxFileSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("존재하지 않는 게시판 설정 수정 시 예외가 발생한다")
    void updateConfig_notFound_throwsException() {
        BoardConfigRequest req = new BoardConfigRequest(
                "수정", "FREE", null, null, "Y", "{}", "Y", 0);
        given(boardConfigRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.updateConfig(999L, req, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("게시판 설정을 삭제한다")
    void deleteConfig_success() {
        given(boardConfigRepository.findById(1L)).willReturn(Optional.of(boardConfig));

        boardService.deleteConfig(1L);

        verify(boardConfigRepository).delete(boardConfig);
    }

    // ===== Post CRUD =====

    @Test
    @DisplayName("게시글 목록을 페이징 조회한다")
    void getPostList_returnsPaged() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<BoardPost> page = new PageImpl<>(List.of(boardPost), pageable, 1);
        given(boardConfigRepository.findById(1L)).willReturn(Optional.of(boardConfig));
        given(boardPostRepository.findByBoardConfig_BoardIdOrderByIsNoticeDescCreatedAtDesc(1L, pageable))
                .willReturn(page);

        Page<BoardPostResponse> result = boardService.getPostList(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("공지 제목");
    }

    @Test
    @DisplayName("게시글을 생성한다")
    void createPost_success() {
        BoardPostCreateRequest req = new BoardPostCreateRequest("새 글", "새 내용", "N");

        given(boardConfigRepository.findById(1L)).willReturn(Optional.of(boardConfig));
        given(boardPostRepository.save(any(BoardPost.class))).willAnswer(inv -> {
            BoardPost saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "postId", 2L);
            return saved;
        });

        BoardPostResponse result = boardService.createPost(1L, req, 1L);

        assertThat(result.getTitle()).isEqualTo("새 글");
        verify(boardPostRepository).save(any(BoardPost.class));
    }

    @Test
    @DisplayName("게시글 상세를 조회하면 조회수가 증가한다")
    void getPostDetail_incrementsViewCnt() {
        given(boardPostRepository.findById(1L)).willReturn(Optional.of(boardPost));

        BoardPostResponse result = boardService.getPostDetail(1L, 1L);

        assertThat(result.getPostId()).isEqualTo(1L);
        assertThat(result.getViewCnt()).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외가 발생한다")
    void getPostDetail_notFound_throwsException() {
        given(boardPostRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.getPostDetail(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글을 수정한다")
    void updatePost_success() {
        BoardPostUpdateRequest req = new BoardPostUpdateRequest("수정 제목", "수정 내용", "N");

        given(boardPostRepository.findById(1L)).willReturn(Optional.of(boardPost));

        BoardPostResponse result = boardService.updatePost(1L, 1L, req, 1L);

        assertThat(result.getTitle()).isEqualTo("수정 제목");
        assertThat(result.getContent()).isEqualTo("수정 내용");
    }

    @Test
    @DisplayName("게시글을 삭제한다")
    void deletePost_success() {
        given(boardPostRepository.findById(1L)).willReturn(Optional.of(boardPost));

        boardService.deletePost(1L, 1L);

        verify(boardPostRepository).delete(boardPost);
    }

    // ===== Comment CRUD =====

    @Test
    @DisplayName("댓글 목록을 조회한다")
    void getCommentList_returnsList() {
        BoardComment comment = BoardComment.builder()
                .postId(1L).content("댓글 내용").createdBy(1L).build();
        ReflectionTestUtils.setField(comment, "commentId", 1L);

        given(boardCommentRepository.findByPostIdOrderByCreatedAtAsc(1L)).willReturn(List.of(comment));

        List<BoardCommentResponse> result = boardService.getCommentList(1L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("댓글 내용");
    }

    @Test
    @DisplayName("댓글을 등록한다")
    void createComment_success() {
        given(boardPostRepository.findById(1L)).willReturn(Optional.of(boardPost));
        given(boardCommentRepository.save(any(BoardComment.class))).willAnswer(inv -> {
            BoardComment saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "commentId", 1L);
            return saved;
        });

        BoardCommentResponse result = boardService.createComment(1L, 1L, "새 댓글", 10L);

        assertThat(result.getContent()).isEqualTo("새 댓글");
        assertThat(result.getCreatedBy()).isEqualTo(10L);
    }

    @Test
    @DisplayName("댓글을 수정한다")
    void updateComment_success() {
        BoardComment comment = BoardComment.builder()
                .postId(1L).content("원래 댓글").createdBy(1L).build();
        ReflectionTestUtils.setField(comment, "commentId", 1L);

        given(boardCommentRepository.findById(1L)).willReturn(Optional.of(comment));

        BoardCommentResponse result = boardService.updateComment(1L, 1L, 1L, "수정된 댓글", 1L);

        assertThat(result.getContent()).isEqualTo("수정된 댓글");
    }

    @Test
    @DisplayName("댓글을 삭제한다")
    void deleteComment_success() {
        BoardComment comment = BoardComment.builder()
                .postId(1L).content("삭제할 댓글").createdBy(1L).build();
        ReflectionTestUtils.setField(comment, "commentId", 1L);

        given(boardCommentRepository.findById(1L)).willReturn(Optional.of(comment));

        boardService.deleteComment(1L, 1L, 1L);

        verify(boardCommentRepository).delete(comment);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 시 예외가 발생한다")
    void deleteComment_notFound_throwsException() {
        given(boardCommentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.deleteComment(1L, 1L, 999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }
}
