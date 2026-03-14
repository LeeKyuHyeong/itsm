package com.itsm.api.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommonCodeGroupCreateRequest {

    @NotBlank(message = "그룹명은 필수입니다.")
    private String groupNm;

    private String groupNmEn;

    @NotBlank(message = "그룹코드는 필수입니다.")
    private String groupCd;

    private String description;
}
