package com.itsm.api.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommonCodeDetailUpdateRequest {

    @NotBlank(message = "코드명은 필수입니다.")
    private String codeNm;

    private String codeNmEn;

    private Integer sortOrder;
}
