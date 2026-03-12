package com.itsm.api.dto.inspection;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InspectionItemRequest {

    @NotBlank(message = "항목명은 필수입니다.")
    private String itemNm;

    private Integer sortOrder;

    private String isRequired;
}
