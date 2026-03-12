package com.itsm.core.domain.inspection;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class InspectionAssetId implements Serializable {
    private Long inspectionId;
    private String assetType;
    private Long assetId;
}
