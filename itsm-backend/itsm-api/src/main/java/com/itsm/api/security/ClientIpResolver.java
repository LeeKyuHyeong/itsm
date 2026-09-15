package com.itsm.api.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * 프록시 뒤 실제 클라이언트 IP 해석기.
 * <p>
 * 2026-09-16 전수조사 P1: 로그인 레이트리밋·접근 로그·감사 로그가 {@code request.getRemoteAddr()} 를 써서
 * 호스트 nginx → 컨테이너 nginx 두 겹 뒤에서는 항상 컨테이너 nginx IP 였다 ("IP당 분당 10회" 가 전 사용자 합산).
 * X-Forwarded-For 해석은 MenuAccessInterceptor 에만 있던 것을 여기로 모았다.
 * <p>
 * 신뢰 프록시(security.trusted-proxies, CIDR 허용)에서 온 요청만 X-Forwarded-For → X-Real-IP 를 믿고,
 * 그 밖에서 온 요청의 헤더는 스푸핑으로 보고 무시한다. 결과는 절대 null 이 아니다 (tb_access_log.ip_address NOT NULL).
 */
@Slf4j
@Component
public class ClientIpResolver {

    private static final String UNKNOWN = "unknown";

    private final List<String> trustedProxies;

    public ClientIpResolver(@Value("${security.trusted-proxies:127.0.0.1,::1}") List<String> trustedProxies) {
        this.trustedProxies = trustedProxies == null ? List.of() : trustedProxies;
    }

    public String resolve(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String fallback = remoteAddr != null && !remoteAddr.isBlank() ? remoteAddr : UNKNOWN;

        if (!isTrustedProxy(remoteAddr)) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                log.warn("Untrusted proxy {} attempted X-Forwarded-For spoofing: {}", remoteAddr, xff);
            }
            return fallback;
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String[] ips = xForwardedFor.split(",");
            // 오른쪽에서 왼쪽으로 신뢰 프록시를 건너뛰고 첫 비신뢰 IP 반환
            for (int i = ips.length - 1; i >= 0; i--) {
                String ip = ips[i].trim();
                if (!ip.isEmpty() && !isTrustedProxy(ip)) {
                    return ip;
                }
            }
            String first = ips[0].trim();
            return first.isEmpty() ? fallback : first;
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return fallback;
    }

    private boolean isTrustedProxy(String ip) {
        if (ip == null) {
            return false;
        }
        for (String trusted : trustedProxies) {
            String t = trusted.trim();
            if (t.contains("/")) {
                if (isInCidrRange(ip, t)) {
                    return true;
                }
            } else if (t.equals(ip)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInCidrRange(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            byte[] networkBytes = InetAddress.getByName(parts[0]).getAddress();
            int prefixLength = Integer.parseInt(parts[1]);
            byte[] ipBytes = InetAddress.getByName(ip).getAddress();
            if (networkBytes.length != ipBytes.length) {
                return false;
            }
            int fullBytes = prefixLength / 8;
            int remainBits = prefixLength % 8;
            for (int i = 0; i < fullBytes; i++) {
                if (networkBytes[i] != ipBytes[i]) {
                    return false;
                }
            }
            if (remainBits > 0 && fullBytes < networkBytes.length) {
                int mask = 0xFF << (8 - remainBits);
                return (networkBytes[fullBytes] & mask) == (ipBytes[fullBytes] & mask);
            }
            return true;
        } catch (UnknownHostException | NumberFormatException e) {
            log.warn("Invalid CIDR notation: {}", cidr, e);
            return false;
        }
    }
}
