package com.itsm.api.dto.asset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssetHwUpdateRequest {

    @NotBlank(message = "자산명은 필수입니다.")
    @Size(max = 100, message = "자산명은 100자 이하여야 합니다.")
    private String assetNm;

    @NotBlank(message = "자산유형코드는 필수입니다.")
    @Size(max = 50, message = "자산유형코드는 50자 이하여야 합니다.")
    private String assetTypeCd;

    @Size(max = 20)
    private String assetCategory;
    @Size(max = 30)
    private String assetSubCategory;

    @Size(max = 100)
    private String manufacturer;
    @Size(max = 100)
    private String modelNm;
    @Size(max = 100)
    private String serialNo;

    @Size(max = 50, message = "IP 주소는 50자 이하여야 합니다.")
    @Pattern(regexp = "^$|^(\\d{1,3}\\.){3}\\d{1,3}(/\\d{1,2})?$|^[0-9a-fA-F:]+$",
            message = "유효한 IP 주소 형식이 아닙니다.")
    private String ipAddress;

    @Size(max = 50, message = "MAC 주소는 50자 이하여야 합니다.")
    @Pattern(regexp = "^$|^([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}$",
            message = "유효한 MAC 주소 형식이 아닙니다. (예: AA:BB:CC:DD:EE:FF)")
    private String macAddress;

    @Size(max = 200)
    private String location;
    private LocalDate introducedAt;
    private LocalDate warrantyEndAt;
    private Long managerId;

    @Size(max = 2000, message = "설명은 2000자 이하여야 합니다.")
    private String description;
}
