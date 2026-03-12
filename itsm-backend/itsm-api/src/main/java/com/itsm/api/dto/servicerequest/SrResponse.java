package com.itsm.api.dto.servicerequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrResponse {

    private Long requestId;
    private String title;
    private String content;
    private String requestTypeCd;
    private String priorityCd;
    private String statusCd;
    private LocalDateTime occurredAt;
    private LocalDateTime completedAt;
    private LocalDateTime closedAt;
    private LocalDateTime slaDeadlineAt;
    private int rejectCnt;
    private Long companyId;
    private String companyNm;
    private Integer satisfactionScore;
    private String satisfactionComment;
    private Double slaPercentage;
    private int assigneeCount;
    private int completedAssigneeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
