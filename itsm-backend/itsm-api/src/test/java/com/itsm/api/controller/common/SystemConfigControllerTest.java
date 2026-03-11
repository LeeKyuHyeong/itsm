package com.itsm.api.controller.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.common.SystemConfigResponse;
import com.itsm.api.dto.common.SystemConfigUpdateRequest;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.common.SystemConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SystemConfigControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SystemConfigService systemConfigService;

    @InjectMocks
    private SystemConfigController systemConfigController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(systemConfigController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/admin/system-configs - 전체 시스템 설정 조회 시 200을 반환한다")
    void getAllConfigs_returns200() throws Exception {
        // given
        SystemConfigResponse response = SystemConfigResponse.builder()
                .configId(1L)
                .configKey("site.name")
                .configVal("ITSM System")
                .description("사이트 이름")
                .updatedAt(LocalDateTime.now())
                .build();

        given(systemConfigService.getAllConfigs()).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/admin/system-configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].configId").value(1))
                .andExpect(jsonPath("$.data[0].configKey").value("site.name"))
                .andExpect(jsonPath("$.data[0].configVal").value("ITSM System"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/system-configs/{configKey} - 시스템 설정 수정 시 200을 반환한다")
    void updateConfig_returns200() throws Exception {
        // given
        SystemConfigUpdateRequest req = new SystemConfigUpdateRequest("New ITSM System");

        SystemConfigResponse response = SystemConfigResponse.builder()
                .configId(1L)
                .configKey("site.name")
                .configVal("New ITSM System")
                .description("사이트 이름")
                .updatedAt(LocalDateTime.now())
                .build();

        given(systemConfigService.updateConfig(eq("site.name"), any(SystemConfigUpdateRequest.class), eq(1L)))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/admin/system-configs/site.name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.configVal").value("New ITSM System"));
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
