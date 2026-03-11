package com.itsm.core.domain.company;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_company_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CompanyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "company_nm", nullable = false, length = 100)
    private String companyNm;

    @Column(name = "biz_no", length = 20)
    private String bizNo;

    @Column(name = "ceo_nm", length = 50)
    private String ceoNm;

    @Column(name = "changed_field", length = 100)
    private String changedField;

    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Builder
    public CompanyHistory(Long companyId, String companyNm, String bizNo, String ceoNm,
                          String changedField, String beforeValue, String afterValue, Long createdBy) {
        this.companyId = companyId;
        this.companyNm = companyNm;
        this.bizNo = bizNo;
        this.ceoNm = ceoNm;
        this.changedField = changedField;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.createdBy = createdBy;
    }
}
