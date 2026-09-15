package com.itsm.api.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * 2026-09-16 전수조사 P1 — 로그인 레이트리밋·접근 로그·감사 로그가 request.getRemoteAddr() 를 써서
 * 두 겹 nginx 뒤에서는 항상 컨테이너 nginx IP 였다("IP당 분당 10회" 가 실제로는 전 사용자 합산).
 * X-Forwarded-For 해석은 MenuAccessInterceptor 에만 있던 것을 공통 컴포넌트로 옮긴다.
 */
class ClientIpResolverTest {

    private HttpServletRequest request(String remoteAddr, String xff, String xRealIp) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        given(req.getRemoteAddr()).willReturn(remoteAddr);
        given(req.getHeader("X-Forwarded-For")).willReturn(xff);
        given(req.getHeader("X-Real-IP")).willReturn(xRealIp);
        return req;
    }

    @Test
    @DisplayName("신뢰 프록시(172.16.0.0/12)에서 온 요청은 X-Forwarded-For 의 첫 비신뢰 IP 를 돌려준다")
    void trustedProxy_usesForwardedFor() {
        ClientIpResolver resolver = new ClientIpResolver(List.of("127.0.0.1", "::1", "172.16.0.0/12"));

        assertThat(resolver.resolve(request("172.19.0.3", "203.0.113.7, 172.19.0.1", null)))
                .isEqualTo("203.0.113.7");
    }

    @Test
    @DisplayName("X-Forwarded-For 가 없으면 X-Real-IP, 그것도 없으면 remoteAddr")
    void fallbacks() {
        ClientIpResolver resolver = new ClientIpResolver(List.of("172.16.0.0/12"));

        assertThat(resolver.resolve(request("172.19.0.3", null, "198.51.100.4"))).isEqualTo("198.51.100.4");
        assertThat(resolver.resolve(request("172.19.0.3", null, null))).isEqualTo("172.19.0.3");
    }

    @Test
    @DisplayName("신뢰하지 않는 주소가 보낸 X-Forwarded-For/X-Real-IP 는 무시한다 (스푸핑)")
    void untrustedProxy_ignoresHeaders() {
        ClientIpResolver resolver = new ClientIpResolver(List.of("127.0.0.1"));

        assertThat(resolver.resolve(request("203.0.113.50", "10.0.0.1", "10.0.0.2"))).isEqualTo("203.0.113.50");
    }

    @Test
    @DisplayName("체인 전체가 신뢰 프록시면 맨 앞 값을 쓴다")
    void allTrusted_usesFirst() {
        ClientIpResolver resolver = new ClientIpResolver(List.of("10.0.0.0/8"));

        assertThat(resolver.resolve(request("10.0.0.5", "10.0.0.1, 10.0.0.2", null))).isEqualTo("10.0.0.1");
    }

    @Test
    @DisplayName("remoteAddr 이 null 이어도 null 대신 'unknown' (tb_access_log.ip_address NOT NULL)")
    void neverNull() {
        ClientIpResolver resolver = new ClientIpResolver(List.of());

        assertThat(resolver.resolve(request(null, null, null))).isEqualTo("unknown");
    }
}
