package com.itsm.api.service.batch;

import com.itsm.api.dto.batch.BatchJobResponse;
import com.itsm.api.dto.batch.BatchJobUpdateRequest;
import com.itsm.core.domain.batch.BatchJob;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.repository.batch.BatchJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class BatchJobServiceTest {

    @Mock
    private BatchJobRepository batchJobRepository;

    @InjectMocks
    private BatchJobService batchJobService;

    private BatchJob batchJob;

    @BeforeEach
    void setUp() {
        batchJob = BatchJob.builder()
                .jobName("SlaWarningJob")
                .cronExpression("0 0 * * * *")
                .jobDescription("SLA 경고 알림")
                .build();
        ReflectionTestUtils.setField(batchJob, "batchJobId", 1L);
    }

    @Test
    @DisplayName("모든 배치 작업 목록을 조회한다")
    void getAllJobs_returnsList() {
        given(batchJobRepository.findAllByOrderByJobNameAsc()).willReturn(List.of(batchJob));

        List<BatchJobResponse> result = batchJobService.getAllJobs();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getJobName()).isEqualTo("SlaWarningJob");
    }

    @Test
    @DisplayName("배치 작업 상세를 조회한다")
    void getJob_returnsResponse() {
        given(batchJobRepository.findById(1L)).willReturn(Optional.of(batchJob));

        BatchJobResponse result = batchJobService.getJob(1L);

        assertThat(result.getBatchJobId()).isEqualTo(1L);
        assertThat(result.getCronExpression()).isEqualTo("0 0 * * * *");
    }

    @Test
    @DisplayName("존재하지 않는 배치 작업 조회 시 예외가 발생한다")
    void getJob_notFound_throwsException() {
        given(batchJobRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> batchJobService.getJob(999L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("배치 작업을 수정한다")
    void updateJob_success() {
        BatchJobUpdateRequest req = new BatchJobUpdateRequest("0 30 * * * *", "N", "변경됨");
        given(batchJobRepository.findById(1L)).willReturn(Optional.of(batchJob));

        BatchJobResponse result = batchJobService.updateJob(1L, req, 1L);

        assertThat(result.getCronExpression()).isEqualTo("0 30 * * * *");
        assertThat(result.getIsActive()).isEqualTo("N");
    }
}
