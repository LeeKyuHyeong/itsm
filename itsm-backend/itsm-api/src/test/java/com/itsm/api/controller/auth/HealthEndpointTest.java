package com.itsm.api.controller.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 배포 헬스체크(deploy.yml)가 호출하는 /api/v1/auth/health 계약.
 * - 인증 없이 200 이어야 한다 (permitAll + 인터셉터 제외 경로).
 * - 이 엔드포인트가 없으면 배포 스크립트는 401/404 를 "healthy" 로 오판한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/auth/health - 인증 없이 200과 status=UP 을 반환한다")
    void health_withoutAuth_returns200Up() throws Exception {
        mockMvc.perform(get("/api/v1/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/health - 잘못된 토큰이 있어도 200 (헬스체크는 인증과 무관)")
    void health_withInvalidToken_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/auth/health")
                        .header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isOk());
    }
}
