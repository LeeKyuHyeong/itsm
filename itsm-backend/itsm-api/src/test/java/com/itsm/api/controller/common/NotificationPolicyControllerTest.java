package com.itsm.api.controller.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.common.NotificationPolicyCreateRequest;
import com.itsm.api.dto.common.NotificationPolicyResponse;
import com.itsm.api.dto.common.NotificationPolicyUpdateRequest;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.common.NotificationPolicyService;
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
class NotificationPolicyControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private NotificationPolicyService notificationPolicyService;

    @InjectMocks
    private NotificationPolicyController notificationPolicyController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(notificationPolicyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/admin/notification-policies - 전체 알림 정책 조회 시 200을 반환한다")
    void getAllPolicies_returns200() throws Exception {
        // given
        NotificationPolicyResponse response = NotificationPolicyResponse.builder()
                .policyId(1L)
                .notiTypeCd("SLA_WARNING")
                .triggerCondition("SLA 경과율 80% 초과")
                .targetRoleCd("ROLE_PM")
                .isActive("Y")
                .createdAt(LocalDateTime.now())
                .build();
        given(notificationPolicyService.getAllPolicies()).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/admin/notification-policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].policyId").value(1))
                .andExpect(jsonPath("$.data[0].notiTypeCd").value("SLA_WARNING"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/notification-policies?notiTypeCd=SLA_WARNING - 유형별 알림 정책 조회 시 200을 반환한다")
    void getPoliciesByType_returns200() throws Exception {
        // given
        NotificationPolicyResponse response = NotificationPolicyResponse.builder()
                .policyId(1L)
                .notiTypeCd("SLA_WARNING")
                .triggerCondition("SLA 경과율 80% 초과")
                .targetRoleCd("ROLE_PM")
                .isActive("Y")
                .createdAt(LocalDateTime.now())
                .build();
        given(notificationPolicyService.getPoliciesByType("SLA_WARNING")).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/admin/notification-policies")
                        .param("notiTypeCd", "SLA_WARNING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].notiTypeCd").value("SLA_WARNING"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/notification-policies - 알림 정책 생성 시 200을 반환한다")
    void createPolicy_returns200() throws Exception {
        // given
        NotificationPolicyCreateRequest req = new NotificationPolicyCreateRequest(
                "INCIDENT_ASSIGN", "장애 담당자 배정 시", "ROLE_ENGINEER");
        NotificationPolicyResponse response = NotificationPolicyResponse.builder()
                .policyId(2L)
                .notiTypeCd("INCIDENT_ASSIGN")
                .triggerCondition("장애 담당자 배정 시")
                .targetRoleCd("ROLE_ENGINEER")
                .isActive("Y")
                .createdAt(LocalDateTime.now())
                .build();
        given(notificationPolicyService.createPolicy(any(NotificationPolicyCreateRequest.class), eq(1L)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/admin/notification-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.policyId").value(2))
                .andExpect(jsonPath("$.data.notiTypeCd").value("INCIDENT_ASSIGN"))
                .andExpect(jsonPath("$.data.targetRoleCd").value("ROLE_ENGINEER"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/notification-policies/{id} - 알림 정책 수정 시 200을 반환한다")
    void updatePolicy_returns200() throws Exception {
        // given
        NotificationPolicyUpdateRequest req = new NotificationPolicyUpdateRequest(
                "SLA 경과율 90% 초과", "ROLE_ADMIN");
        NotificationPolicyResponse response = NotificationPolicyResponse.builder()
                .policyId(1L)
                .notiTypeCd("SLA_WARNING")
                .triggerCondition("SLA 경과율 90% 초과")
                .targetRoleCd("ROLE_ADMIN")
                .isActive("Y")
                .createdAt(LocalDateTime.now())
                .build();
        given(notificationPolicyService.updatePolicy(eq(1L), any(NotificationPolicyUpdateRequest.class), eq(1L)))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/admin/notification-policies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.triggerCondition").value("SLA 경과율 90% 초과"))
                .andExpect(jsonPath("$.data.targetRoleCd").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/notification-policies/{id}/status - 알림 정책 상태 변경 시 200을 반환한다")
    void changePolicyStatus_returns200() throws Exception {
        // given & when & then
        mockMvc.perform(patch("/api/v1/admin/notification-policies/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("isActive", "N"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationPolicyService).changePolicyStatus(1L, "N");
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
