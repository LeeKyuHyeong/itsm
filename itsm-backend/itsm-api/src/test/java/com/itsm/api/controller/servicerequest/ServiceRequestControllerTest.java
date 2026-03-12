package com.itsm.api.controller.servicerequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.servicerequest.*;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.servicerequest.ServiceRequestService;
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
class ServiceRequestControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ServiceRequestService serviceRequestService;

    @InjectMocks
    private ServiceRequestController serviceRequestController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(serviceRequestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/service-requests - 서비스요청 목록 조회 시 200을 반환한다")
    void search_returns200() throws Exception {
        SrResponse response = SrResponse.builder()
                .requestId(1L).title("프린터 설치 요청").statusCd("RECEIVED")
                .priorityCd("MEDIUM").companyId(1L).companyNm("테스트회사")
                .createdAt(LocalDateTime.now()).build();
        Page<SrResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        given(serviceRequestService.search(any(), any(), any(), any(), any(), any())).willReturn(page);

        mockMvc.perform(get("/api/v1/service-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].requestId").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("프린터 설치 요청"));
    }

    @Test
    @DisplayName("GET /api/v1/service-requests/{id} - 서비스요청 상세 조회 시 200을 반환한다")
    void getDetail_returns200() throws Exception {
        SrResponse response = SrResponse.builder()
                .requestId(1L).title("프린터 설치 요청").statusCd("RECEIVED")
                .priorityCd("MEDIUM").requestTypeCd("INSTALL")
                .companyId(1L).companyNm("테스트회사")
                .createdAt(LocalDateTime.now()).build();
        given(serviceRequestService.getDetail(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/service-requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestId").value(1))
                .andExpect(jsonPath("$.data.title").value("프린터 설치 요청"));
    }

    @Test
    @DisplayName("POST /api/v1/service-requests - 서비스요청 생성 시 200을 반환한다")
    void create_returns200() throws Exception {
        SrCreateRequest req = new SrCreateRequest(
                "프린터 설치 요청", "3층 프린터 설치 부탁드립니다", "INSTALL", "MEDIUM",
                LocalDateTime.of(2026, 3, 13, 10, 0), 1L);
        SrResponse response = SrResponse.builder()
                .requestId(1L).title("프린터 설치 요청").statusCd("RECEIVED")
                .createdAt(LocalDateTime.now()).build();
        given(serviceRequestService.create(any(SrCreateRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/service-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestId").value(1));
    }

    @Test
    @DisplayName("PATCH /api/v1/service-requests/{id} - 서비스요청 수정 시 200을 반환한다")
    void update_returns200() throws Exception {
        SrUpdateRequest req = new SrUpdateRequest(
                "프린터 설치 요청 - 수정", "내용 수정", "REPAIR", "HIGH",
                LocalDateTime.of(2026, 3, 13, 11, 0));
        SrResponse response = SrResponse.builder()
                .requestId(1L).title("프린터 설치 요청 - 수정").statusCd("RECEIVED")
                .createdAt(LocalDateTime.now()).build();
        given(serviceRequestService.update(eq(1L), any(SrUpdateRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(patch("/api/v1/service-requests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("프린터 설치 요청 - 수정"));
    }

    @Test
    @DisplayName("PATCH /api/v1/service-requests/{id}/status - 상태 변경 시 200을 반환한다")
    void changeStatus_returns200() throws Exception {
        mockMvc.perform(patch("/api/v1/service-requests/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ASSIGNED")))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(serviceRequestService).changeStatus(1L, "ASSIGNED", 1L);
    }

    @Test
    @DisplayName("POST /api/v1/service-requests/{id}/assignees - 담당자 지정 시 200을 반환한다")
    void assignUser_returns200() throws Exception {
        SrAssigneeResponse response = SrAssigneeResponse.builder()
                .requestId(1L).userId(10L).userNm("테스트유저")
                .processStatus("PENDING").grantedAt(LocalDateTime.now()).build();
        given(serviceRequestService.assignUser(eq(1L), eq(10L), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/service-requests/1/assignees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("userId", 10)))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(10));
    }

    @Test
    @DisplayName("DELETE /api/v1/service-requests/{id}/assignees/{userId} - 담당자 해제 시 200을 반환한다")
    void removeAssignee_returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/service-requests/1/assignees/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(serviceRequestService).removeAssignee(1L, 10L);
    }

    @Test
    @DisplayName("GET /api/v1/service-requests/{id}/assignees - 담당자 목록 조회 시 200을 반환한다")
    void getAssignees_returns200() throws Exception {
        SrAssigneeResponse response = SrAssigneeResponse.builder()
                .requestId(1L).userId(10L).userNm("테스트유저")
                .processStatus("PENDING").grantedAt(LocalDateTime.now()).build();
        given(serviceRequestService.getAssignees(1L)).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/service-requests/1/assignees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userId").value(10));
    }

    @Test
    @DisplayName("POST /api/v1/service-requests/{id}/processes - 처리내용 등록 시 200을 반환한다")
    void addProcess_returns200() throws Exception {
        SrProcessResponse response = SrProcessResponse.builder()
                .processId(1L).requestId(1L).userId(10L)
                .processContent("프린터 설치 진행 중").isCompleted("N")
                .createdAt(LocalDateTime.now()).build();
        given(serviceRequestService.addProcess(eq(1L), eq(10L), eq("프린터 설치 진행 중"))).willReturn(response);

        mockMvc.perform(post("/api/v1/service-requests/1/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("userId", 10, "processContent", "프린터 설치 진행 중")))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.processContent").value("프린터 설치 진행 중"));
    }

    @Test
    @DisplayName("PATCH /api/v1/service-requests/{id}/processes/{processId}/complete - 처리완료 시 200을 반환한다")
    void completeProcess_returns200() throws Exception {
        mockMvc.perform(patch("/api/v1/service-requests/1/processes/1/complete")
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(serviceRequestService).completeProcess(1L, 1L);
    }

    @Test
    @DisplayName("GET /api/v1/service-requests/{id}/processes - 처리내용 목록 조회 시 200을 반환한다")
    void getProcesses_returns200() throws Exception {
        SrProcessResponse response = SrProcessResponse.builder()
                .processId(1L).requestId(1L).userId(10L)
                .processContent("진행 중").isCompleted("N")
                .createdAt(LocalDateTime.now()).build();
        given(serviceRequestService.getProcesses(1L)).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/service-requests/1/processes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].processContent").value("진행 중"));
    }

    @Test
    @DisplayName("POST /api/v1/service-requests/{id}/satisfaction - 만족도 제출 시 200을 반환한다")
    void submitSatisfaction_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/service-requests/1/satisfaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("score", 5, "comment", "만족합니다")))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(serviceRequestService).submitSatisfaction(1L, 5, "만족합니다");
    }

    @Test
    @DisplayName("GET /api/v1/service-requests/{id}/history - 변경이력 조회 시 200을 반환한다")
    void getHistory_returns200() throws Exception {
        SrHistoryResponse history = SrHistoryResponse.builder()
                .historyId(1L).changedField("statusCd")
                .beforeValue("RECEIVED").afterValue("ASSIGNED")
                .createdBy(1L).createdAt(LocalDateTime.now()).build();
        given(serviceRequestService.getHistory(1L)).willReturn(List.of(history));

        mockMvc.perform(get("/api/v1/service-requests/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].changedField").value("statusCd"));
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
