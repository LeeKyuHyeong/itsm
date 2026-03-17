package com.itsm.api.dto.change;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalDecisionRequest {

    @NotBlank(message = "승인/반려 결정은 필수입니다.")
    private String decision;

    private String comment;
}
