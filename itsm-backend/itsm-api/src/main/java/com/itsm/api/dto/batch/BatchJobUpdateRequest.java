package com.itsm.api.dto.batch;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @NoArgsConstructor @AllArgsConstructor
public class BatchJobUpdateRequest {
    @NotBlank(message = "CRON 표현식은 필수입니다.")
    private String cronExpression;
    @NotBlank(message = "활성 여부는 필수입니다.")
    private String isActive;
    private String jobDescription;
    private String jobNameEn;
}
