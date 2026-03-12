package com.itsm.api.dto.inspection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InspectionUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "점검유형코드는 필수입니다.")
    private String inspectionTypeCd;

    @NotNull(message = "예정일은 필수입니다.")
    private LocalDate scheduledAt;

    private Long managerId;

    private String description;
}
