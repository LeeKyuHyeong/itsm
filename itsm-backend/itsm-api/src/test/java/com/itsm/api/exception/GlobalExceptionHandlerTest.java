package com.itsm.api.exception;

import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestExceptionController testExceptionController() {
            return new TestExceptionController();
        }
    }

    @RestController
    static class TestExceptionController {

        @GetMapping("/test/business-exception")
        public void throwBusinessException() {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }

        @GetMapping("/test/exception")
        public void throwException() {
            throw new RuntimeException("unexpected error");
        }
    }

    @Test
    @DisplayName("BusinessException 발생 시 적절한 에러 응답을 반환한다")
    @WithMockUser
    void handleBusinessException() throws Exception {
        mockMvc.perform(get("/test/business-exception"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("E404_001"))
                .andExpect(jsonPath("$.error.message").value("대상을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("일반 Exception 발생 시 500 에러 응답을 반환한다")
    @WithMockUser
    void handleException() throws Exception {
        mockMvc.perform(get("/test/exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("E500_001"));
    }
}
