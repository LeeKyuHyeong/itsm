package com.itsm.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginRateLimiter 단위 테스트")
class LoginRateLimiterTest {

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter();
    }

    @Test
    @DisplayName("아무 시도도 없으면 isBlocked는 false를 반환한다")
    void isBlocked_noAttempts_returnsFalse() {
        assertThat(rateLimiter.isBlocked("127.0.0.1")).isFalse();
    }

    @Test
    @DisplayName("9회 이하 시도는 차단되지 않는다")
    void recordAttempt_under10_notBlocked() {
        String ip = "127.0.0.1";
        for (int i = 0; i < 9; i++) {
            rateLimiter.recordAttempt(ip);
        }
        assertThat(rateLimiter.isBlocked(ip)).isFalse();
    }

    @Test
    @DisplayName("10회 시도 시 차단된다")
    void recordAttempt_10times_blocked() {
        String ip = "127.0.0.1";
        for (int i = 0; i < 10; i++) {
            rateLimiter.recordAttempt(ip);
        }
        assertThat(rateLimiter.isBlocked(ip)).isTrue();
    }

    @Test
    @DisplayName("10회 초과해도 계속 차단 상태를 유지한다")
    void recordAttempt_over10_remainsBlocked() {
        String ip = "127.0.0.1";
        for (int i = 0; i < 15; i++) {
            rateLimiter.recordAttempt(ip);
        }
        assertThat(rateLimiter.isBlocked(ip)).isTrue();
    }

    @Test
    @DisplayName("IP 별로 카운터가 독립적으로 관리된다")
    void recordAttempt_perIpIsolation() {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";

        for (int i = 0; i < 10; i++) {
            rateLimiter.recordAttempt(ip1);
        }

        assertThat(rateLimiter.isBlocked(ip1)).isTrue();
        assertThat(rateLimiter.isBlocked(ip2)).isFalse();
    }
}
