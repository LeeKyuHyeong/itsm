package com.itsm.api.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigUpdateRequest {

    @NotBlank(message = "설정 값은 필수입니다.")
    private String configVal;
}
