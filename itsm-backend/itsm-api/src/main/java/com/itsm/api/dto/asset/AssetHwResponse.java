package com.itsm.api.dto.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetHwResponse {

    private Long assetHwId;
    private String assetNm;
    private String assetTypeCd;
    private String manufacturer;
    private String modelNm;
    private String serialNo;
    private String ipAddress;
    private String macAddress;
    private String location;
    private LocalDate introducedAt;
    private LocalDate warrantyEndAt;
    private Long companyId;
    private String companyNm;
    private Long managerId;
    private String managerNm;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
