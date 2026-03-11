package com.itsm.api.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPolicyUpdateRequest {

    @NotBlank(message = "트리거 조건은 필수입니다.")
    private String triggerCondition;

    @NotBlank(message = "대상 역할 코드는 필수입니다.")
    private String targetRoleCd;
}
