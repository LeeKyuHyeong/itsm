package com.itsm.core.domain.asset;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AssetRelationId implements Serializable {

    private Long assetHwId;
    private Long assetSwId;
}
