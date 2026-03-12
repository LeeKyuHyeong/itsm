package com.itsm.api.dto.asset;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssetRelationRequest {

    @NotNull(message = "HW 자산 ID는 필수입니다.")
    private Long assetHwId;

    @NotNull(message = "SW 자산 ID는 필수입니다.")
    private Long assetSwId;

    private LocalDate installedAt;
}
