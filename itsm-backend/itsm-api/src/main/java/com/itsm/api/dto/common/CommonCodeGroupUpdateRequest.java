package com.itsm.api.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommonCodeGroupUpdateRequest {

    @NotBlank(message = "그룹명은 필수입니다.")
    private String groupNm;

    private String description;
}
