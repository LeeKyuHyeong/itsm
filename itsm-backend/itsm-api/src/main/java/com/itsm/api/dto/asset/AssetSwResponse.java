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
public class AssetSwResponse {

    private Long assetSwId;
    private String swNm;
    private String swTypeCd;
    private String version;
    private String licenseKey;
    private Integer licenseCnt;
    private LocalDate installedAt;
    private LocalDate expiredAt;
    private Long companyId;
    private String companyNm;
    private Long managerId;
    private String managerNm;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
