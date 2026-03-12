package com.itsm.api.dto.report;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportFormRequest {

    @NotBlank(message = "양식명은 필수입니다.")
    private String formNm;

    @NotBlank(message = "양식유형코드는 필수입니다.")
    private String formTypeCd;

    @NotBlank(message = "양식 스키마는 필수입니다.")
    private String formSchema;

    private String isActive;
}
