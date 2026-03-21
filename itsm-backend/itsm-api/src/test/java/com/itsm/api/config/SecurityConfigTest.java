package com.itsm.api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Swagger UI는 인증 없이 접근 가능하다")
    void swaggerAccessible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인증되지 않은 요청은 거부된다")
    void unauthenticatedRequestIsDenied() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("BCryptPasswordEncoder가 빈으로 등록되어 있다")
    void passwordEncoderBean() {
        assertThat(passwordEncoder).isNotNull();
        String encoded = passwordEncoder.encode("test1234");
        assertThat(passwordEncoder.matches("test1234", encoded)).isTrue();
    }

    @Test
    @DisplayName("응답에 X-Frame-Options 헤더가 포함된다")
    void responseContainsXFrameOptions() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @DisplayName("응답에 X-Content-Type-Options 헤더가 포함된다")
    void responseContainsXContentTypeOptions() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @DisplayName("응답에 Referrer-Policy 헤더가 포함된다")
    void responseContainsReferrerPolicy() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"));
    }

    @Test
    @DisplayName("응답에 X-XSS-Protection 헤더가 포함된다")
    void responseContainsXXssProtection() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-XSS-Protection", "0"));
    }

    @Test
    @DisplayName("응답에 Content-Security-Policy 헤더가 포함된다")
    void responseContainsContentSecurityPolicy() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    @DisplayName("HTTPS 요청 시 Strict-Transport-Security 헤더가 포함된다")
    void responseContainsHstsHeader() throws Exception {
        mockMvc.perform(get("/v3/api-docs").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().exists("Strict-Transport-Security"));
    }
}
