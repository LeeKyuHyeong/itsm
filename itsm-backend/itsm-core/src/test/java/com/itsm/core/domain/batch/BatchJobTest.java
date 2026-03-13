package com.itsm.core.domain.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BatchJobTest {

    @Test
    @DisplayName("Builder로 BatchJob 생성 시 기본값이 설정된다")
    void builder_defaultValues() {
        BatchJob job = BatchJob.builder()
                .jobName("TestJob")
                .cronExpression("0 0 * * * *")
                .jobDescription("테스트 배치")
                .build();

        assertThat(job.getJobName()).isEqualTo("TestJob");
        assertThat(job.getCronExpression()).isEqualTo("0 0 * * * *");
        assertThat(job.getIsActive()).isEqualTo("Y");
    }

    @Test
    @DisplayName("update로 CRON과 활성여부를 변경한다")
    void update_changesFields() {
        BatchJob job = BatchJob.builder()
                .jobName("TestJob")
                .cronExpression("0 0 * * * *")
                .build();

        job.update("0 30 * * * *", "N", "변경된 설명");

        assertThat(job.getCronExpression()).isEqualTo("0 30 * * * *");
        assertThat(job.getIsActive()).isEqualTo("N");
        assertThat(job.getJobDescription()).isEqualTo("변경된 설명");
    }

    @Test
    @DisplayName("recordExecution으로 실행 결과를 기록한다")
    void recordExecution_updatesResult() {
        BatchJob job = BatchJob.builder()
                .jobName("TestJob")
                .cronExpression("0 0 * * * *")
                .build();

        job.recordExecution("SUCCESS", "3건 처리");

        assertThat(job.getLastExecutedAt()).isNotNull();
        assertThat(job.getLastResult()).isEqualTo("SUCCESS");
        assertThat(job.getLastResultMessage()).isEqualTo("3건 처리");
    }

    @Test
    @DisplayName("isEnabled는 isActive가 Y일 때 true를 반환한다")
    void isEnabled_returnsCorrectly() {
        BatchJob active = BatchJob.builder().jobName("A").cronExpression("0 0 * * * *").isActive("Y").build();
        BatchJob inactive = BatchJob.builder().jobName("B").cronExpression("0 0 * * * *").isActive("N").build();

        assertThat(active.isEnabled()).isTrue();
        assertThat(inactive.isEnabled()).isFalse();
    }
}
