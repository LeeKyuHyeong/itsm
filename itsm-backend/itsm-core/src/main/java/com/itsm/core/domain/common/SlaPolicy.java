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
@Table(name = "tb_sla_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "priority_cd", nullable = false, length = 20)
    private String priorityCd;

    @Column(name = "deadline_hours", nullable = false)
    private Integer deadlineHours;

    @Column(name = "warning_pct", nullable = false)
    private Integer warningPct;

    @Column(name = "is_active", nullable = false, columnDefinition = "char(1)")
    private String isActive;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Builder
    public SlaPolicy(Long companyId, String priorityCd, Integer deadlineHours,
                     Integer warningPct, String isActive, Long createdBy) {
        this.companyId = companyId;
        this.priorityCd = priorityCd;
        this.deadlineHours = deadlineHours;
        this.warningPct = warningPct != null ? warningPct : 80;
        this.isActive = isActive != null ? isActive : "Y";
        this.createdBy = createdBy;
    }

    public void update(Integer deadlineHours, Integer warningPct) {
        this.deadlineHours = deadlineHours;
        this.warningPct = warningPct;
    }

    public void activate() {
        this.isActive = "Y";
    }

    public void deactivate() {
        this.isActive = "N";
    }
}
