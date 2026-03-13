package com.itsm.core.domain.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginHistory 엔티티 테스트")
class LoginHistoryTest {

    @Test
    @DisplayName("로그인 이력 생성")
    void createLoginHistory() {
        LocalDateTime loginAt = LocalDateTime.of(2026, 3, 13, 9, 0, 0);

        LoginHistory history = LoginHistory.builder()
                .userId(1L)
                .loginAt(loginAt)
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0")
                .build();

        assertThat(history.getUserId()).isEqualTo(1L);
        assertThat(history.getLoginAt()).isEqualTo(loginAt);
        assertThat(history.getIpAddress()).isEqualTo("192.168.1.100");
        assertThat(history.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(history.getLogoutAt()).isNull();
    }

    @Test
    @DisplayName("로그아웃 기록")
    void recordLogout() {
        LocalDateTime loginAt = LocalDateTime.of(2026, 3, 13, 9, 0, 0);
        LocalDateTime logoutAt = LocalDateTime.of(2026, 3, 13, 18, 0, 0);

        LoginHistory history = LoginHistory.builder()
                .userId(1L)
                .loginAt(loginAt)
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0")
                .build();

        history.recordLogout(logoutAt);

        assertThat(history.getLogoutAt()).isEqualTo(logoutAt);
    }
}
