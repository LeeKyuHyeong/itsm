package com.itsm.core.domain.log;

import com.itsm.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_login_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "login_history_id")
    private Long loginHistoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt;

    @Column(name = "logout_at")
    private LocalDateTime logoutAt;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 300)
    private String userAgent;

    @Builder
    public LoginHistory(Long userId, LocalDateTime loginAt, String ipAddress, String userAgent) {
        this.userId = userId;
        this.loginAt = loginAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public void recordLogout(LocalDateTime logoutAt) {
        this.logoutAt = logoutAt;
    }
}
