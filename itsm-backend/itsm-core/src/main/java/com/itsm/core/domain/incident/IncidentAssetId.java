package com.itsm.core.domain.incident;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class IncidentAssetId implements Serializable {
    private Long incidentId;
    private String assetType;
    private Long assetId;
}
