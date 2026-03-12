package com.itsm.core.domain.servicerequest;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ServiceRequestAssetId implements Serializable {
    private Long requestId;
    private String assetType;
    private Long assetId;
}
