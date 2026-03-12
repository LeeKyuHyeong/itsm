package com.itsm.api.dto.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentAssetResponse {

    private Long incidentId;
    private String assetType;
    private Long assetId;
    private String assetNm;
    private LocalDateTime createdAt;
}
