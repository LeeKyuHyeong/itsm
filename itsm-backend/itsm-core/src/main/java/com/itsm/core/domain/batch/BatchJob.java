package com.itsm.core.domain.batch;

import com.itsm.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_batch_job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_job_id")
    private Long batchJobId;

    @Column(name = "job_name", nullable = false, unique = true, length = 100)
    private String jobName;

    @Column(name = "job_description", length = 300)
    private String jobDescription;

    @Column(name = "cron_expression", nullable = false, length = 50)
    private String cronExpression;

    @Column(name = "is_active", nullable = false, length = 1)
    private String isActive;

    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;

    @Column(name = "last_result", length = 20)
    private String lastResult;

    @Column(name = "last_result_message", columnDefinition = "TEXT")
    private String lastResultMessage;

    @Builder
    public BatchJob(String jobName, String jobDescription, String cronExpression, String isActive) {
        this.jobName = jobName;
        this.jobDescription = jobDescription;
        this.cronExpression = cronExpression;
        this.isActive = isActive != null ? isActive : "Y";
    }

    public void update(String cronExpression, String isActive, String jobDescription) {
        this.cronExpression = cronExpression;
        this.isActive = isActive;
        this.jobDescription = jobDescription;
    }

    public void recordExecution(String result, String message) {
        this.lastExecutedAt = LocalDateTime.now();
        this.lastResult = result;
        this.lastResultMessage = message;
    }

    public boolean isEnabled() {
        return "Y".equals(this.isActive);
    }
}
