package com.itsm.api.dto.change;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeResponse {

    private Long changeId;
    private String title;
    private String content;
    private String changeTypeCd;
    private String priorityCd;
    private String statusCd;
    private LocalDateTime scheduledAt;
    private String rollbackPlan;
    private Long companyId;
    private String companyNm;
    private int approverCount;
    private int approvedCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
