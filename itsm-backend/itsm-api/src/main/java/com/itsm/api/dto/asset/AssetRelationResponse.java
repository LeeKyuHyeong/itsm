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
public class AssetRelationResponse {

    private Long assetHwId;
    private String assetHwNm;
    private Long assetSwId;
    private String assetSwNm;
    private LocalDate installedAt;
    private LocalDate removedAt;
    private LocalDateTime createdAt;
}
