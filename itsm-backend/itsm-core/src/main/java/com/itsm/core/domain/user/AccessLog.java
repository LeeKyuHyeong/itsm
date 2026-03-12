package com.itsm.core.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_access_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", length = 50)
    private String loginId;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    @Column(name = "success_yn", nullable = false, columnDefinition = "char(1)")
    private String successYn;

    @Column(name = "fail_reason", length = 200)
    private String failReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AccessLog(Long userId, String loginId, String actionType,
                     String ipAddress, String successYn, String failReason) {
        this.userId = userId;
        this.loginId = loginId;
        this.actionType = actionType;
        this.ipAddress = ipAddress;
        this.successYn = successYn;
        this.failReason = failReason;
    }
}
