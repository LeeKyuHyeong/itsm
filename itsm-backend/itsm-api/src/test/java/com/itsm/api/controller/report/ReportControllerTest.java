package com.itsm.api.controller.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.report.*;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.report.ReportService;
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
class ReportControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(reportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    // ===== ReportForm =====

    @Test
    @DisplayName("GET /api/v1/report-forms - 양식 목록 조회 시 200을 반환한다")
    void searchForms_returns200() throws Exception {
        ReportFormResponse response = ReportFormResponse.builder()
                .formId(1L).formNm("점검 보고서 양식").formTypeCd("INSPECTION")
                .formSchema("{\"fields\": []}").isActive("Y")
                .createdAt(LocalDateTime.now()).build();
        Page<ReportFormResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        given(reportService.searchForms(any(), any(), any(), any())).willReturn(page);

        mockMvc.perform(get("/api/v1/report-forms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].formId").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/report-forms/{id} - 양식 상세 조회 시 200을 반환한다")
    void getFormDetail_returns200() throws Exception {
        ReportFormResponse response = ReportFormResponse.builder()
                .formId(1L).formNm("점검 보고서 양식").formTypeCd("INSPECTION")
                .formSchema("{\"fields\": []}").isActive("Y")
                .createdAt(LocalDateTime.now()).build();
        given(reportService.getFormDetail(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/report-forms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.formId").value(1))
                .andExpect(jsonPath("$.data.formNm").value("점검 보고서 양식"));
    }

    @Test
    @DisplayName("POST /api/v1/report-forms - 양식 생성 시 200을 반환한다")
    void createForm_returns200() throws Exception {
        ReportFormRequest req = new ReportFormRequest(
                "점검 보고서 양식", "INSPECTION", "{\"fields\": []}", "Y");
        ReportFormResponse response = ReportFormResponse.builder()
                .formId(1L).formNm("점검 보고서 양식").formTypeCd("INSPECTION")
                .createdAt(LocalDateTime.now()).build();
        given(reportService.createForm(any(ReportFormRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/report-forms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.formId").value(1));
    }

    @Test
    @DisplayName("PATCH /api/v1/report-forms/{id} - 양식 수정 시 200을 반환한다")
    void updateForm_returns200() throws Exception {
        ReportFormRequest req = new ReportFormRequest(
                "수정된 양식", "CHANGE", "{\"fields\": [\"new\"]}", "N");
        ReportFormResponse response = ReportFormResponse.builder()
                .formId(1L).formNm("수정된 양식").formTypeCd("CHANGE")
                .createdAt(LocalDateTime.now()).build();
        given(reportService.updateForm(eq(1L), any(ReportFormRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(patch("/api/v1/report-forms/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.formNm").value("수정된 양식"));
    }

    @Test
    @DisplayName("DELETE /api/v1/report-forms/{id} - 양식 삭제 시 200을 반환한다")
    void deleteForm_returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/report-forms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(reportService).deleteForm(1L);
    }

    // ===== Report =====

    @Test
    @DisplayName("GET /api/v1/reports - 보고서 목록 조회 시 200을 반환한다")
    void searchReports_returns200() throws Exception {
        ReportResponse response = ReportResponse.builder()
                .reportId(1L).formId(1L).formNm("점검 보고서 양식")
                .refType("INSPECTION").refId(1L)
                .reportContent("{\"result\": \"pass\"}")
                .createdAt(LocalDateTime.now()).build();
        Page<ReportResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        given(reportService.searchReports(any(), any(), any())).willReturn(page);

        mockMvc.perform(get("/api/v1/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].reportId").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/reports/{id} - 보고서 상세 조회 시 200을 반환한다")
    void getReportDetail_returns200() throws Exception {
        ReportResponse response = ReportResponse.builder()
                .reportId(1L).formId(1L).formNm("점검 보고서 양식")
                .refType("INSPECTION").refId(1L)
                .reportContent("{\"result\": \"pass\"}")
                .createdAt(LocalDateTime.now()).build();
        given(reportService.getReportDetail(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").value(1))
                .andExpect(jsonPath("$.data.formNm").value("점검 보고서 양식"));
    }

    @Test
    @DisplayName("POST /api/v1/reports - 보고서 생성 시 200을 반환한다")
    void createReport_returns200() throws Exception {
        ReportCreateRequest req = new ReportCreateRequest(
                1L, "INSPECTION", 1L, "{\"result\": \"pass\"}");
        ReportResponse response = ReportResponse.builder()
                .reportId(1L).formId(1L).formNm("점검 보고서 양식")
                .refType("INSPECTION").refId(1L)
                .createdAt(LocalDateTime.now()).build();
        given(reportService.createReport(any(ReportCreateRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").value(1));
    }

    @Test
    @DisplayName("PATCH /api/v1/reports/{id} - 보고서 수정 시 200을 반환한다")
    void updateReport_returns200() throws Exception {
        ReportResponse response = ReportResponse.builder()
                .reportId(1L).formId(1L).formNm("점검 보고서 양식")
                .refType("INSPECTION").refId(1L)
                .reportContent("{\"result\": \"updated\"}")
                .createdAt(LocalDateTime.now()).build();
        given(reportService.updateReport(eq(1L), eq("{\"result\": \"updated\"}"), eq(1L))).willReturn(response);

        mockMvc.perform(patch("/api/v1/reports/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reportContent", "{\"result\": \"updated\"}")))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportContent").value("{\"result\": \"updated\"}"));
    }

    @Test
    @DisplayName("DELETE /api/v1/reports/{id} - 보고서 삭제 시 200을 반환한다")
    void deleteReport_returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(reportService).deleteReport(1L);
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
