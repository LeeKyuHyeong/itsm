package com.itsm.core.domain.inspection;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_inspection_asset")
@IdClass(InspectionAssetId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class InspectionAsset {

    @Id
    @Column(name = "inspection_id")
    private Long inspectionId;

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
    public InspectionAsset(Long inspectionId, String assetType, Long assetId, Long createdBy) {
        this.inspectionId = inspectionId;
        this.assetType = assetType;
        this.assetId = assetId;
        this.createdBy = createdBy;
    }
}
