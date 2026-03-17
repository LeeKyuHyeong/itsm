package com.itsm.api.dto.servicerequest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SrSatisfactionRequest {

    @NotNull(message = "만족도 점수는 필수입니다.")
    @Min(value = 1, message = "최소 1점 이상이어야 합니다.")
    @Max(value = 5, message = "최대 5점까지 가능합니다.")
    private Integer score;

    private String comment;
}
