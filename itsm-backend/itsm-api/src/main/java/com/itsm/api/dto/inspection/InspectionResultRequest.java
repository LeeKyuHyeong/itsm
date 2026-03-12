package com.itsm.api.dto.inspection;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InspectionResultRequest {

    @NotNull(message = "항목ID는 필수입니다.")
    private Long itemId;

    private String resultValue;

    private String isNormal;

    private String remark;
}
