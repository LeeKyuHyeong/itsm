package com.itsm.core.domain.servicerequest;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_service_request_asset")
@IdClass(ServiceRequestAssetId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ServiceRequestAsset {

    @Id
    @Column(name = "request_id")
    private Long requestId;

    @Id
    @Column(name = "asset_type", length = 10)
    private String assetType;

    @Id
    @Column(name = "asset_id")
    private Long assetId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Builder
    public ServiceRequestAsset(Long requestId, String assetType, Long assetId, Long createdBy) {
        this.requestId = requestId;
        this.assetType = assetType;
        this.assetId = assetId;
        this.createdBy = createdBy;
    }
}
