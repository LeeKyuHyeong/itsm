package com.itsm.api.dto.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SlaPolicyCreateRequest {

    private Long companyId;

    @NotBlank(message = "우선순위 코드는 필수입니다.")
    private String priorityCd;

    @NotNull(message = "처리기한(시간)은 필수입니다.")
    private Integer deadlineHours;

    private Integer warningPct;
}
