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
@Table(name = "tb_inspection_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class InspectionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "inspection_id", nullable = false)
    private Long inspectionId;

    @Column(name = "changed_field", nullable = false, length = 100)
    private String changedField;

    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Builder
    public InspectionHistory(Long inspectionId, String changedField, String beforeValue,
                             String afterValue, Long createdBy) {
        this.inspectionId = inspectionId;
        this.changedField = changedField;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.createdBy = createdBy;
    }
}
