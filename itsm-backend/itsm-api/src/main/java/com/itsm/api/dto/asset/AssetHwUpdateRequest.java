package com.itsm.api.dto.asset;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssetHwUpdateRequest {

    @NotBlank(message = "자산명은 필수입니다.")
    private String assetNm;

    @NotBlank(message = "자산유형코드는 필수입니다.")
    private String assetTypeCd;

    private String assetCategory;
    private String assetSubCategory;

    private String manufacturer;
    private String modelNm;
    private String serialNo;
    private String ipAddress;
    private String macAddress;
    private String location;
    private LocalDate introducedAt;
    private LocalDate warrantyEndAt;
    private Long managerId;
    private String description;
}
