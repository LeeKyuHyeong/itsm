package com.itsm.api.dto.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IncidentAssetRequest {

    @NotBlank(message = "자산유형은 필수입니다.")
    private String assetType;

    @NotNull(message = "자산ID는 필수입니다.")
    private Long assetId;
}
