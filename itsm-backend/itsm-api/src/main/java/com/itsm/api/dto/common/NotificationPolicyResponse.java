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
public class NotificationPolicyResponse {

    private Long policyId;
    private String notiTypeCd;
    private String triggerCondition;
    private String targetRoleCd;
    private String isActive;
    private LocalDateTime createdAt;
}
