package com.itsm.api.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1qdW5pdC10ZXN0aW5nLW9ubHktbWluaW11bS0yNTYtYml0cy1sb25n";
    private static final long ACCESS_TOKEN_EXPIRY = 1800000L;
    private static final long REFRESH_TOKEN_EXPIRY = 604800000L;

    private final JwtBlacklist jwtBlacklist = new JwtBlacklist();

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, ACCESS_TOKEN_EXPIRY, REFRESH_TOKEN_EXPIRY, jwtBlacklist);
    }

    @Test
    @DisplayName("Access 토큰을 생성하면 유효한 토큰이 반환된다")
    void createAccessToken_returnsValidToken() {
        // given
        Long userId = 1L;
        String loginId = "admin";
        List<String> roles = List.of("ADMIN", "USER");

        // when
        String token = jwtTokenProvider.createAccessToken(userId, loginId, roles);

        // then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Access 토큰의 Claims를 정확히 파싱한다")
    void parseClaims_extractsCorrectClaims() {
        // given
        Long userId = 1L;
        String loginId = "admin";
        List<String> roles = List.of("ADMIN", "USER");
        String token = jwtTokenProvider.createAccessToken(userId, loginId, roles);

        // when
        Claims claims = jwtTokenProvider.parseClaims(token);

        // then
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("loginId", String.class)).isEqualTo("admin");
        assertThat(claims.get("type", String.class)).isEqualTo("ACCESS");
        assertThat(claims.get("roles", List.class)).containsExactly("ADMIN", "USER");
    }

    @Test
    @DisplayName("만료된 토큰은 validateToken이 false를 반환한다")
    void validateToken_returnsFalse_forExpiredToken() {
        // given
        JwtTokenProvider shortExpiryProvider = new JwtTokenProvider(SECRET, -1000L, -1000L, jwtBlacklist);
        String token = shortExpiryProvider.createAccessToken(1L, "admin", List.of("ADMIN"));

        // when & then
        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("유효하지 않은 토큰은 validateToken이 false를 반환한다")
    void validateToken_returnsFalse_forInvalidToken() {
        // when & then
        assertThat(jwtTokenProvider.validateToken("invalid.token.value")).isFalse();
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("토큰에서 userId를 추출한다")
    void getUserId_extractsUserId() {
        // given
        String token = jwtTokenProvider.createAccessToken(42L, "user1", List.of("USER"));

        // when
        Long userId = jwtTokenProvider.getUserId(token);

        // then
        assertThat(userId).isEqualTo(42L);
    }

    @Test
    @DisplayName("토큰에서 loginId를 추출한다")
    void getLoginId_extractsLoginId() {
        // given
        String token = jwtTokenProvider.createAccessToken(1L, "testuser", List.of("USER"));

        // when
        String loginId = jwtTokenProvider.getLoginId(token);

        // then
        assertThat(loginId).isEqualTo("testuser");
    }

    @Test
    @DisplayName("토큰에서 roles를 추출한다")
    void getRoles_extractsRoles() {
        // given
        List<String> expectedRoles = List.of("ADMIN", "USER");
        String token = jwtTokenProvider.createAccessToken(1L, "admin", expectedRoles);

        // when
        List<String> roles = jwtTokenProvider.getRoles(token);

        // then
        assertThat(roles).containsExactly("ADMIN", "USER");
    }

    @Test
    @DisplayName("Refresh 토큰을 생성하면 유효한 토큰이 반환된다")
    void createRefreshToken_returnsValidToken() {
        // given
        Long userId = 1L;

        // when
        String token = jwtTokenProvider.createRefreshToken(userId);

        // then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();

        Claims claims = jwtTokenProvider.parseClaims(token);
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("type", String.class)).isEqualTo("REFRESH");
    }

    @Test
    @DisplayName("invalidate 호출 후 토큰은 더 이상 유효하지 않다 (블랙리스트)")
    void invalidate_blacklistedToken_failsValidation() {
        // given
        String token = jwtTokenProvider.createAccessToken(1L, "admin", List.of("ADMIN"));
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();

        // when
        jwtTokenProvider.invalidate(token);

        // then
        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("다른 secret으로 서명된 토큰은 validateToken이 false를 반환한다 (변조 방지)")
    void validateToken_tamperedSignature_returnsFalse() {
        // given
        String otherSecret = "ZGlmZmVyZW50LXNlY3JldC1rZXktZm9yLXRhbXBlcmluZy10ZXN0LW1pbmltdW0tMjU2LWJpdHMtbG9uZw==";
        JwtTokenProvider otherProvider = new JwtTokenProvider(
                otherSecret, ACCESS_TOKEN_EXPIRY, REFRESH_TOKEN_EXPIRY, new JwtBlacklist());
        String tampered = otherProvider.createAccessToken(1L, "admin", List.of("ADMIN"));

        // when & then
        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("getAccessTokenExpirySeconds는 밀리초를 초로 변환하여 반환한다")
    void getAccessTokenExpirySeconds_returnsConvertedValue() {
        assertThat(jwtTokenProvider.getAccessTokenExpirySeconds()).isEqualTo(1800L);
    }

    @Test
    @DisplayName("유효하지 않은 토큰을 invalidate해도 예외가 발생하지 않는다")
    void invalidate_invalidToken_doesNotThrow() {
        // 만료된 토큰이나 변조된 토큰도 invalidate 호출 시 silent하게 처리
        jwtTokenProvider.invalidate("not.a.valid.token");
        jwtTokenProvider.invalidate("");
    }
}
