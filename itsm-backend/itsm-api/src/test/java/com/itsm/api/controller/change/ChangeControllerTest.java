package com.itsm.api.controller.change;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.change.*;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.change.ChangeService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChangeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ChangeService changeService;

    @InjectMocks
    private ChangeController changeController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(changeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/changes - 변경 목록 조회 시 200을 반환한다")
    void search_returns200() throws Exception {
        ChangeResponse response = ChangeResponse.builder()
                .changeId(1L).title("서버 패치 적용").statusCd("DRAFT")
                .priorityCd("MEDIUM").companyId(1L).companyNm("테스트회사")
                .createdAt(LocalDateTime.now()).build();
        Page<ChangeResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        given(changeService.search(any(), any(), any(), any(), any(), any())).willReturn(page);

        mockMvc.perform(get("/api/v1/changes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].changeId").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/changes/{id} - 변경 상세 조회 시 200을 반환한다")
    void getDetail_returns200() throws Exception {
        ChangeResponse response = ChangeResponse.builder()
                .changeId(1L).title("서버 패치 적용").statusCd("DRAFT")
                .priorityCd("MEDIUM").changeTypeCd("NORMAL")
                .companyId(1L).companyNm("테스트회사")
                .createdAt(LocalDateTime.now()).build();
        given(changeService.getDetail(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/changes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.changeId").value(1))
                .andExpect(jsonPath("$.data.title").value("서버 패치 적용"));
    }

    @Test
    @DisplayName("POST /api/v1/changes - 변경 생성 시 200을 반환한다")
    void create_returns200() throws Exception {
        ChangeCreateRequest req = new ChangeCreateRequest(
                "서버 패치 적용", "보안 패치 적용", "NORMAL", "MEDIUM",
                LocalDateTime.of(2026, 3, 20, 10, 0), "이전 버전 복원", 1L);
        ChangeResponse response = ChangeResponse.builder()
                .changeId(1L).title("서버 패치 적용").statusCd("DRAFT")
                .createdAt(LocalDateTime.now()).build();
        given(changeService.create(any(ChangeCreateRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.changeId").value(1));
    }

    @Test
    @DisplayName("PATCH /api/v1/changes/{id} - 변경 수정 시 200을 반환한다")
    void update_returns200() throws Exception {
        ChangeUpdateRequest req = new ChangeUpdateRequest(
                "패치 적용 수정", "내용 수정", "EMERGENCY", "HIGH",
                LocalDateTime.of(2026, 3, 21, 10, 0), "수정된 롤백 계획");
        ChangeResponse response = ChangeResponse.builder()
                .changeId(1L).title("패치 적용 수정").statusCd("DRAFT")
                .createdAt(LocalDateTime.now()).build();
        given(changeService.update(eq(1L), any(ChangeUpdateRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(patch("/api/v1/changes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("패치 적용 수정"));
    }

    @Test
    @DisplayName("PATCH /api/v1/changes/{id}/status - 상태 변경 시 200을 반환한다")
    void changeStatus_returns200() throws Exception {
        mockMvc.perform(patch("/api/v1/changes/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "APPROVAL_REQUESTED")))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(changeService).changeStatus(1L, "APPROVAL_REQUESTED", 1L);
    }

    @Test
    @DisplayName("POST /api/v1/changes/{id}/approvers - 승인자 추가 시 200을 반환한다")
    void addApprover_returns200() throws Exception {
        ChangeApproverResponse response = ChangeApproverResponse.builder()
                .changeId(1L).userId(10L).userNm("매니저")
                .approveOrder(1).approveStatus("PENDING").build();
        given(changeService.addApprover(eq(1L), eq(10L), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/changes/1/approvers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("userId", 10)))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(10))
                .andExpect(jsonPath("$.data.approveOrder").value(1));
    }

    @Test
    @DisplayName("PATCH /api/v1/changes/{id}/approvers/{userId} - 승인/반려 시 200을 반환한다")
    void approveChange_returns200() throws Exception {
        mockMvc.perform(patch("/api/v1/changes/1/approvers/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("decision", "APPROVED", "comment", "승인합니다")))
                        .principal(createAuthentication(10L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(changeService).approveChange(1L, 10L, "APPROVED", "승인합니다", 10L);
    }

    @Test
    @DisplayName("POST /api/v1/changes/{id}/comments - 댓글 등록 시 200을 반환한다")
    void addComment_returns200() throws Exception {
        ChangeCommentResponse response = ChangeCommentResponse.builder()
                .commentId(1L).changeId(1L).content("확인 중")
                .createdBy(1L).createdAt(LocalDateTime.now()).build();
        given(changeService.addComment(eq(1L), eq("확인 중"), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/changes/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "확인 중")))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("확인 중"));
    }

    @Test
    @DisplayName("GET /api/v1/changes/{id}/history - 변경이력 조회 시 200을 반환한다")
    void getHistory_returns200() throws Exception {
        ChangeHistoryResponse history = ChangeHistoryResponse.builder()
                .historyId(1L).changedField("statusCd")
                .beforeValue("DRAFT").afterValue("APPROVAL_REQUESTED")
                .createdBy(1L).createdAt(LocalDateTime.now()).build();
        given(changeService.getHistory(1L)).willReturn(List.of(history));

        mockMvc.perform(get("/api/v1/changes/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].changedField").value("statusCd"));
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
