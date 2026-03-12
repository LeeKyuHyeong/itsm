package com.itsm.core.domain.change;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChangeAssetId implements Serializable {
    private Long changeId;
    private String assetType;
    private Long assetId;
}
