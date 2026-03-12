package com.itsm.core.domain.asset;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_asset_relation")
@IdClass(AssetRelationId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AssetRelation {

    @Id
    @Column(name = "asset_hw_id")
    private Long assetHwId;

    @Id
    @Column(name = "asset_sw_id")
    private Long assetSwId;

    @Column(name = "installed_at")
    private LocalDate installedAt;

    @Column(name = "removed_at")
    private LocalDate removedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_hw_id", insertable = false, updatable = false)
    private AssetHw assetHw;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_sw_id", insertable = false, updatable = false)
    private AssetSw assetSw;

    @Builder
    public AssetRelation(Long assetHwId, Long assetSwId, LocalDate installedAt, Long createdBy) {
        this.assetHwId = assetHwId;
        this.assetSwId = assetSwId;
        this.installedAt = installedAt;
        this.createdBy = createdBy;
    }

    public void remove() {
        this.removedAt = LocalDate.now();
    }
}
