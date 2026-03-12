package com.itsm.core.domain.asset;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_asset_hw_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AssetHwHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "asset_hw_id", nullable = false)
    private Long assetHwId;

    @Column(name = "changed_field", nullable = false, length = 100)
    private String changedField;

    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;

    @Column(name = "batch_job_id", length = 50)
    private String batchJobId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Builder
    public AssetHwHistory(Long assetHwId, String changedField, String beforeValue,
                          String afterValue, String batchJobId, Long createdBy) {
        this.assetHwId = assetHwId;
        this.changedField = changedField;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.batchJobId = batchJobId;
        this.createdBy = createdBy;
    }
}
