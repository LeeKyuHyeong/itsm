package com.itsm.api.dto.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {

    private Long incidentId;
    private String title;
    private String content;
    private String incidentTypeCd;
    private String priorityCd;
    private String statusCd;
    private LocalDateTime occurredAt;
    private LocalDateTime completedAt;
    private LocalDateTime closedAt;
    private LocalDateTime slaDeadlineAt;
    private Long companyId;
    private String companyNm;
    private Long mainManagerId;
    private String mainManagerNm;
    private String processContent;
    private Double slaPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
