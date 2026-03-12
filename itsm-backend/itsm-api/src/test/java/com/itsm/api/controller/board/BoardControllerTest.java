package com.itsm.api.controller.board;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.board.*;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.board.BoardService;
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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BoardControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private BoardService boardService;

    @InjectMocks
    private BoardController boardController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(boardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    // ===== Board Config =====

    @Test
    @DisplayName("GET /api/v1/admin/boards - 게시판 설정 목록 조회")
    void getConfigList_returns200() throws Exception {
        BoardConfigResponse response = BoardConfigResponse.builder()
                .boardId(1L).boardNm("공지사항").boardTypeCd("NOTICE")
                .allowComment("Y").isActive("Y").sortOrder(1)
                .createdAt(LocalDateTime.now()).build();
        given(boardService.getConfigList()).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/admin/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].boardNm").value("공지사항"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/boards - 게시판 설정 생성")
    void createConfig_returns200() throws Exception {
        BoardConfigRequest req = new BoardConfigRequest(
                "자유게시판", "FREE", "jpg,png", 5, "Y", "{}", "Y", 2);
        BoardConfigResponse response = BoardConfigResponse.builder()
                .boardId(2L).boardNm("자유게시판").boardTypeCd("FREE")
                .allowComment("Y").isActive("Y").sortOrder(2)
                .createdAt(LocalDateTime.now()).build();
        given(boardService.createConfig(any(BoardConfigRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/admin/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.boardNm").value("자유게시판"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/boards/{boardId} - 게시판 설정 수정")
    void updateConfig_returns200() throws Exception {
        BoardConfigRequest req = new BoardConfigRequest(
                "수정된 공지", "NOTICE", "pdf", 20, "Y", "{}", "Y", 1);
        BoardConfigResponse response = BoardConfigResponse.builder()
                .boardId(1L).boardNm("수정된 공지").boardTypeCd("NOTICE")
                .allowComment("Y").isActive("Y").sortOrder(1)
                .createdAt(LocalDateTime.now()).build();
        given(boardService.updateConfig(eq(1L), any(BoardConfigRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(put("/api/v1/admin/boards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.boardNm").value("수정된 공지"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/boards/{boardId} - 게시판 설정 삭제")
    void deleteConfig_returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/boards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(boardService).deleteConfig(1L);
    }

    // ===== Posts =====

    @Test
    @DisplayName("GET /api/v1/boards/{boardId}/posts - 게시글 목록 조회")
    void getPostList_returns200() throws Exception {
        BoardPostResponse response = BoardPostResponse.builder()
                .postId(1L).boardId(1L).boardNm("공지사항")
                .title("공지 제목").viewCnt(0).isNotice("Y")
                .createdAt(LocalDateTime.now()).build();
        Page<BoardPostResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        given(boardService.getPostList(eq(1L), any())).willReturn(page);

        mockMvc.perform(get("/api/v1/boards/1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("공지 제목"));
    }

    @Test
    @DisplayName("POST /api/v1/boards/{boardId}/posts - 게시글 생성")
    void createPost_returns200() throws Exception {
        BoardPostCreateRequest req = new BoardPostCreateRequest("새 글", "내용", "N");
        BoardPostResponse response = BoardPostResponse.builder()
                .postId(2L).boardId(1L).boardNm("공지사항")
                .title("새 글").content("내용").viewCnt(0).isNotice("N")
                .createdAt(LocalDateTime.now()).build();
        given(boardService.createPost(eq(1L), any(BoardPostCreateRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/boards/1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("새 글"));
    }

    @Test
    @DisplayName("GET /api/v1/boards/{boardId}/posts/{postId} - 게시글 상세 조회")
    void getPostDetail_returns200() throws Exception {
        BoardPostResponse response = BoardPostResponse.builder()
                .postId(1L).boardId(1L).boardNm("공지사항")
                .title("공지 제목").content("공지 내용").viewCnt(1).isNotice("Y")
                .createdAt(LocalDateTime.now()).build();
        given(boardService.getPostDetail(1L, 1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/boards/1/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("공지 제목"))
                .andExpect(jsonPath("$.data.viewCnt").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/boards/{boardId}/posts/{postId} - 게시글 수정")
    void updatePost_returns200() throws Exception {
        BoardPostUpdateRequest req = new BoardPostUpdateRequest("수정 제목", "수정 내용", "N");
        BoardPostResponse response = BoardPostResponse.builder()
                .postId(1L).boardId(1L).boardNm("공지사항")
                .title("수정 제목").content("수정 내용").viewCnt(0).isNotice("N")
                .createdAt(LocalDateTime.now()).build();
        given(boardService.updatePost(eq(1L), eq(1L), any(BoardPostUpdateRequest.class), eq(1L)))
                .willReturn(response);

        mockMvc.perform(put("/api/v1/boards/1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정 제목"));
    }

    @Test
    @DisplayName("DELETE /api/v1/boards/{boardId}/posts/{postId} - 게시글 삭제")
    void deletePost_returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/boards/1/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(boardService).deletePost(1L, 1L);
    }

    // ===== Comments =====

    @Test
    @DisplayName("GET /api/v1/boards/{boardId}/posts/{postId}/comments - 댓글 목록 조회")
    void getCommentList_returns200() throws Exception {
        BoardCommentResponse response = BoardCommentResponse.builder()
                .commentId(1L).postId(1L).content("댓글")
                .createdBy(1L).createdAt(LocalDateTime.now()).build();
        given(boardService.getCommentList(1L, 1L)).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/boards/1/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content").value("댓글"));
    }

    @Test
    @DisplayName("POST /api/v1/boards/{boardId}/posts/{postId}/comments - 댓글 등록")
    void createComment_returns200() throws Exception {
        BoardCommentResponse response = BoardCommentResponse.builder()
                .commentId(1L).postId(1L).content("새 댓글")
                .createdBy(1L).createdAt(LocalDateTime.now()).build();
        given(boardService.createComment(eq(1L), eq(1L), eq("새 댓글"), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/boards/1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"새 댓글\"}")
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("새 댓글"));
    }

    @Test
    @DisplayName("PUT /api/v1/boards/{boardId}/posts/{postId}/comments/{commentId} - 댓글 수정")
    void updateComment_returns200() throws Exception {
        BoardCommentResponse response = BoardCommentResponse.builder()
                .commentId(1L).postId(1L).content("수정된 댓글")
                .createdBy(1L).createdAt(LocalDateTime.now()).build();
        given(boardService.updateComment(eq(1L), eq(1L), eq(1L), eq("수정된 댓글"), eq(1L)))
                .willReturn(response);

        mockMvc.perform(put("/api/v1/boards/1/posts/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"수정된 댓글\"}")
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("수정된 댓글"));
    }

    @Test
    @DisplayName("DELETE /api/v1/boards/{boardId}/posts/{postId}/comments/{commentId} - 댓글 삭제")
    void deleteComment_returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/boards/1/posts/1/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(boardService).deleteComment(1L, 1L, 1L);
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
