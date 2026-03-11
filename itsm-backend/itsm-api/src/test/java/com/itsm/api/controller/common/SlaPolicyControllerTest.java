package com.itsm.api.controller.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.common.SlaPolicyCreateRequest;
import com.itsm.api.dto.common.SlaPolicyResponse;
import com.itsm.api.dto.common.SlaPolicyUpdateRequest;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.common.SlaPolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class SlaPolicyControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SlaPolicyService slaPolicyService;

    @InjectMocks
    private SlaPolicyController slaPolicyController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(slaPolicyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/admin/sla-policies - 전체 SLA 정책 조회 시 200을 반환한다")
    void getAllPolicies_returns200() throws Exception {
        // given
        SlaPolicyResponse response = SlaPolicyResponse.builder()
                .policyId(1L)
                .companyId(null)
                .priorityCd("CRITICAL")
                .deadlineHours(4)
                .warningPct(80)
                .isActive("Y")
                .createdAt(LocalDateTime.now())
                .build();
        given(slaPolicyService.getAllPolicies()).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/admin/sla-policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].policyId").value(1))
                .andExpect(jsonPath("$.data[0].priorityCd").value("CRITICAL"))
                .andExpect(jsonPath("$.data[0].deadlineHours").value(4));
    }

    @Test
    @DisplayName("GET /api/v1/admin/sla-policies?companyId=10 - 회사별 SLA 정책 조회 시 200을 반환한다")
    void getPoliciesByCompany_returns200() throws Exception {
        // given
        SlaPolicyResponse response = SlaPolicyResponse.builder()
                .policyId(2L)
                .companyId(10L)
                .priorityCd("HIGH")
                .deadlineHours(8)
                .warningPct(70)
                .isActive("Y")
                .createdAt(LocalDateTime.now())
                .build();
        given(slaPolicyService.getPoliciesByCompany(10L)).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/admin/sla-policies")
                        .param("companyId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].companyId").value(10))
                .andExpect(jsonPath("$.data[0].priorityCd").value("HIGH"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/sla-policies - SLA 정책 생성 시 200을 반환한다")
    void createPolicy_returns200() throws Exception {
        // given
        SlaPolicyCreateRequest req = new SlaPolicyCreateRequest(null, "HIGH", 8, 70);
        SlaPolicyResponse response = SlaPolicyResponse.builder()
                .policyId(2L)
                .companyId(null)
                .priorityCd("HIGH")
                .deadlineHours(8)
                .warningPct(70)
                .isActive("Y")
                .createdAt(LocalDateTime.now())
                .build();
        given(slaPolicyService.createPolicy(any(SlaPolicyCreateRequest.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/admin/sla-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.policyId").value(2))
                .andExpect(jsonPath("$.data.priorityCd").value("HIGH"))
                .andExpect(jsonPath("$.data.deadlineHours").value(8));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/sla-policies/{id} - SLA 정책 수정 시 200을 반환한다")
    void updatePolicy_returns200() throws Exception {
        // given
        SlaPolicyUpdateRequest req = new SlaPolicyUpdateRequest(12, 90);
        SlaPolicyResponse response = SlaPolicyResponse.builder()
                .policyId(1L)
                .companyId(null)
                .priorityCd("CRITICAL")
                .deadlineHours(12)
                .warningPct(90)
                .isActive("Y")
                .createdAt(LocalDateTime.now())
                .build();
        given(slaPolicyService.updatePolicy(eq(1L), any(SlaPolicyUpdateRequest.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/admin/sla-policies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deadlineHours").value(12))
                .andExpect(jsonPath("$.data.warningPct").value(90));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/sla-policies/{id}/status - SLA 정책 상태 변경 시 200을 반환한다")
    void changePolicyStatus_returns200() throws Exception {
        // given & when & then
        mockMvc.perform(patch("/api/v1/admin/sla-policies/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("isActive", "N"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(slaPolicyService).changePolicyStatus(1L, "N");
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
