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
@Table(name = "tb_notification_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class NotificationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "noti_type_cd", nullable = false, length = 50)
    private String notiTypeCd;

    @Column(name = "trigger_condition", nullable = false, length = 200)
    private String triggerCondition;

    @Column(name = "target_role_cd", nullable = false, length = 50)
    private String targetRoleCd;

    @Column(name = "is_active", nullable = false, length = 1)
    private String isActive;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Builder
    public NotificationPolicy(String notiTypeCd, String triggerCondition,
                              String targetRoleCd, String isActive, Long createdBy) {
        this.notiTypeCd = notiTypeCd;
        this.triggerCondition = triggerCondition;
        this.targetRoleCd = targetRoleCd;
        this.isActive = isActive != null ? isActive : "Y";
        this.createdBy = createdBy;
    }

    public void update(String triggerCondition, String targetRoleCd) {
        this.triggerCondition = triggerCondition;
        this.targetRoleCd = targetRoleCd;
    }

    public void activate() {
        this.isActive = "Y";
    }

    public void deactivate() {
        this.isActive = "N";
    }
}
