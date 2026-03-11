package com.itsm.api.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaPolicyResponse {

    private Long policyId;
    private Long companyId;
    private String priorityCd;
    private Integer deadlineHours;
    private Integer warningPct;
    private String isActive;
    private LocalDateTime createdAt;
}
