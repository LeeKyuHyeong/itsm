package com.itsm.api.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.batch.BatchJobResponse;
import com.itsm.api.dto.batch.BatchJobUpdateRequest;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.batch.BatchJobService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class BatchJobControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private BatchJobService batchJobService;

    @InjectMocks
    private BatchJobController batchJobController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(batchJobController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/admin/batch-jobs - 배치 작업 목록 조회 시 200을 반환한다")
    void getAllJobs_returns200() throws Exception {
        BatchJobResponse response = BatchJobResponse.builder()
                .batchJobId(1L).jobName("SlaWarningJob")
                .cronExpression("0 0 * * * *").isActive("Y")
                .createdAt(LocalDateTime.now()).build();
        given(batchJobService.getAllJobs()).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/admin/batch-jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].jobName").value("SlaWarningJob"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/batch-jobs/{id} - 배치 작업 상세 조회 시 200을 반환한다")
    void getJob_returns200() throws Exception {
        BatchJobResponse response = BatchJobResponse.builder()
                .batchJobId(1L).jobName("SlaWarningJob")
                .cronExpression("0 0 * * * *").isActive("Y")
                .createdAt(LocalDateTime.now()).build();
        given(batchJobService.getJob(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/admin/batch-jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobName").value("SlaWarningJob"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/batch-jobs/{id} - 배치 작업 수정 시 200을 반환한다")
    void updateJob_returns200() throws Exception {
        BatchJobUpdateRequest req = new BatchJobUpdateRequest("0 30 * * * *", "N", "변경됨");
        BatchJobResponse response = BatchJobResponse.builder()
                .batchJobId(1L).jobName("SlaWarningJob")
                .cronExpression("0 30 * * * *").isActive("N")
                .createdAt(LocalDateTime.now()).build();
        given(batchJobService.updateJob(eq(1L), any(BatchJobUpdateRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(patch("/api/v1/admin/batch-jobs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(new UsernamePasswordAuthenticationToken(
                                1L, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cronExpression").value("0 30 * * * *"))
                .andExpect(jsonPath("$.data.isActive").value("N"));
    }
}
