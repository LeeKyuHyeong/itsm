package com.itsm.core.domain.common;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_common_code_detail",
        uniqueConstraints = @UniqueConstraint(name = "uk_common_code_detail", columnNames = {"group_id", "code_val"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CommonCodeDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private CommonCode commonCode;

    @Column(name = "code_val", nullable = false, length = 50)
    private String codeVal;

    @Column(name = "code_nm", nullable = false, length = 100)
    private String codeNm;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false, length = 1)
    private String isActive;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Builder
    public CommonCodeDetail(CommonCode commonCode, String codeVal, String codeNm, Integer sortOrder, String isActive, Long createdBy) {
        this.commonCode = commonCode;
        this.codeVal = codeVal;
        this.codeNm = codeNm;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.isActive = isActive != null ? isActive : "Y";
        this.createdBy = createdBy;
    }

    public void update(String codeNm, Integer sortOrder) {
        this.codeNm = codeNm;
        this.sortOrder = sortOrder;
    }

    public void activate() {
        this.isActive = "Y";
    }

    public void deactivate() {
        this.isActive = "N";
    }
}
