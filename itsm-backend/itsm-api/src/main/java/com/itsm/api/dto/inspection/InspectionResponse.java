package com.itsm.api.dto.inspection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionResponse {

    private Long inspectionId;
    private String title;
    private String inspectionTypeCd;
    private String statusCd;
    private LocalDate scheduledAt;
    private LocalDateTime completedAt;
    private LocalDateTime closedAt;
    private Long companyId;
    private String companyNm;
    private Long managerId;
    private String description;
    private int itemCount;
    private int completedItemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
