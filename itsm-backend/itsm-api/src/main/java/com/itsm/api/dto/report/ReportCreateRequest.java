package com.itsm.api.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreateRequest {

    @NotNull(message = "양식ID는 필수입니다.")
    private Long formId;

    @NotBlank(message = "참조유형은 필수입니다.")
    private String refType;

    @NotNull(message = "참조ID는 필수입니다.")
    private Long refId;

    @NotBlank(message = "보고서 내용은 필수입니다.")
    private String reportContent;
}
