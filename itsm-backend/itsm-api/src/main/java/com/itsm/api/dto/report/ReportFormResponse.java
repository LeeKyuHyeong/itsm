package com.itsm.api.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportFormResponse {

    private Long formId;
    private String formNm;
    private String formTypeCd;
    private String formSchema;
    private String isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
