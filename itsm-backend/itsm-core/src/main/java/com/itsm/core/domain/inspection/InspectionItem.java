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
@Table(name = "tb_inspection_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class InspectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "inspection_id", nullable = false)
    private Long inspectionId;

    @Column(name = "item_nm", nullable = false, length = 200)
    private String itemNm;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_required", nullable = false, columnDefinition = "char(1)")
    private String isRequired;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Builder
    public InspectionItem(Long inspectionId, String itemNm, Integer sortOrder, String isRequired, Long createdBy) {
        this.inspectionId = inspectionId;
        this.itemNm = itemNm;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.isRequired = isRequired != null ? isRequired : "Y";
        this.createdBy = createdBy;
    }

    public void update(String itemNm, Integer sortOrder, String isRequired) {
        this.itemNm = itemNm;
        this.sortOrder = sortOrder;
        this.isRequired = isRequired;
    }
}
