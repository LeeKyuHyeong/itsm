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
public class ReportResponse {

    private Long reportId;
    private Long formId;
    private String formNm;
    private String refType;
    private Long refId;
    private String reportContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
