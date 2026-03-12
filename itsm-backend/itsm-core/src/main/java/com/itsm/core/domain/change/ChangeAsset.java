package com.itsm.core.domain.change;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_change_asset")
@IdClass(ChangeAssetId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChangeAsset {

    @Id
    @Column(name = "change_id")
    private Long changeId;

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
    public ChangeAsset(Long changeId, String assetType, Long assetId, Long createdBy) {
        this.changeId = changeId;
        this.assetType = assetType;
        this.assetId = assetId;
        this.createdBy = createdBy;
    }
}
