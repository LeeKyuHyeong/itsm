package com.itsm.api.dto.batch;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class BatchJobResponse {
    private Long batchJobId;
    private String jobName;
    private String jobDescription;
    private String cronExpression;
    private String isActive;
    private LocalDateTime lastExecutedAt;
    private String lastResult;
    private String lastResultMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
