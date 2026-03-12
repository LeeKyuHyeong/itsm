package com.itsm.api.controller.incident;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.incident.*;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.incident.IncidentService;
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
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
class IncidentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private IncidentService incidentService;

    @InjectMocks
    private IncidentController incidentController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(incidentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/incidents - 장애 목록 조회 시 200을 반환한다")
    void search_returns200() throws Exception {
        IncidentResponse response = IncidentResponse.builder()
                .incidentId(1L).title("서버 장애").statusCd("RECEIVED")
                .priorityCd("CRITICAL").companyId(1L).companyNm("테스트회사")
                .createdAt(LocalDateTime.now()).build();
        Page<IncidentResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        given(incidentService.search(any(), any(), any(), any(), any(), any())).willReturn(page);

        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].incidentId").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("서버 장애"));
    }

    @Test
    @DisplayName("GET /api/v1/incidents/{id} - 장애 상세 조회 시 200을 반환한다")
    void getDetail_returns200() throws Exception {
        IncidentResponse response = IncidentResponse.builder()
                .incidentId(1L).title("서버 장애").statusCd("RECEIVED")
                .priorityCd("CRITICAL").incidentTypeCd("SYSTEM_DOWN")
                .companyId(1L).companyNm("테스트회사")
                .createdAt(LocalDateTime.now()).build();
        given(incidentService.getDetail(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/incidents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.incidentId").value(1))
                .andExpect(jsonPath("$.data.title").value("서버 장애"));
    }

    @Test
    @DisplayName("POST /api/v1/incidents - 장애 생성 시 200을 반환한다")
    void create_returns200() throws Exception {
        IncidentCreateRequest req = new IncidentCreateRequest(
                "서버 장애", "메인 서버 다운", "SYSTEM_DOWN", "CRITICAL",
                LocalDateTime.of(2026, 3, 13, 10, 0), 1L, null, null);
        IncidentResponse response = IncidentResponse.builder()
                .incidentId(1L).title("서버 장애").statusCd("RECEIVED")
                .createdAt(LocalDateTime.now()).build();
        given(incidentService.create(any(IncidentCreateRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.incidentId").value(1));
    }

    @Test
    @DisplayName("PATCH /api/v1/incidents/{id} - 장애 수정 시 200을 반환한다")
    void update_returns200() throws Exception {
        IncidentUpdateRequest req = new IncidentUpdateRequest(
                "서버 장애 - 수정", "내용 수정", "NETWORK_ISSUE", "HIGH",
                LocalDateTime.of(2026, 3, 13, 11, 0), null);
        IncidentResponse response = IncidentResponse.builder()
                .incidentId(1L).title("서버 장애 - 수정").statusCd("RECEIVED")
                .createdAt(LocalDateTime.now()).build();
        given(incidentService.update(eq(1L), any(IncidentUpdateRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(patch("/api/v1/incidents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("서버 장애 - 수정"));
    }

    @Test
    @DisplayName("PATCH /api/v1/incidents/{id}/status - 상태 변경 시 200을 반환한다")
    void changeStatus_returns200() throws Exception {
        mockMvc.perform(patch("/api/v1/incidents/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "IN_PROGRESS")))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(incidentService).changeStatus(1L, "IN_PROGRESS", 1L);
    }

    @Test
    @DisplayName("POST /api/v1/incidents/{id}/assignees - 담당자 지정 시 200을 반환한다")
    void assignUser_returns200() throws Exception {
        IncidentAssigneeResponse response = IncidentAssigneeResponse.builder()
                .incidentId(1L).userId(10L).userNm("매니저")
                .grantedAt(LocalDateTime.now()).build();
        given(incidentService.assignUser(eq(1L), eq(10L), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/incidents/1/assignees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("userId", 10)))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(10));
    }

    @Test
    @DisplayName("DELETE /api/v1/incidents/{id}/assignees/{userId} - 담당자 해제 시 200을 반환한다")
    void removeAssignee_returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/incidents/1/assignees/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(incidentService).removeAssignee(1L, 10L);
    }

    @Test
    @DisplayName("POST /api/v1/incidents/{id}/comments - 댓글 등록 시 200을 반환한다")
    void addComment_returns200() throws Exception {
        IncidentCommentResponse response = IncidentCommentResponse.builder()
                .commentId(1L).incidentId(1L).content("확인 중")
                .createdBy(1L).createdAt(LocalDateTime.now()).build();
        given(incidentService.addComment(eq(1L), eq("확인 중"), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/incidents/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "확인 중")))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("확인 중"));
    }

    @Test
    @DisplayName("GET /api/v1/incidents/{id}/history - 변경이력 조회 시 200을 반환한다")
    void getHistory_returns200() throws Exception {
        IncidentHistoryResponse history = IncidentHistoryResponse.builder()
                .historyId(1L).changedField("statusCd")
                .beforeValue("RECEIVED").afterValue("IN_PROGRESS")
                .createdBy(1L).createdAt(LocalDateTime.now()).build();
        given(incidentService.getHistory(1L)).willReturn(List.of(history));

        mockMvc.perform(get("/api/v1/incidents/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].changedField").value("statusCd"));
    }

    @Test
    @DisplayName("POST /api/v1/incidents/{id}/report - 장애보고서 작성 시 200을 반환한다")
    void saveReport_returns200() throws Exception {
        IncidentReportRequest req = new IncidentReportRequest(1L, "{\"summary\":\"요약\"}");
        IncidentReportResponse response = IncidentReportResponse.builder()
                .reportId(1L).incidentId(1L).reportFormId(1L)
                .reportContent("{\"summary\":\"요약\"}")
                .createdBy(1L).createdAt(LocalDateTime.now()).build();
        given(incidentService.saveReport(eq(1L), any(IncidentReportRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/incidents/1/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").value(1));
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
