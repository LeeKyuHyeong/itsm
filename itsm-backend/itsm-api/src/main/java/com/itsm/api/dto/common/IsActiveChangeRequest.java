package com.itsm.api.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IsActiveChangeRequest {

    @NotBlank(message = "활성화 여부는 필수입니다.")
    private String isActive;
}
