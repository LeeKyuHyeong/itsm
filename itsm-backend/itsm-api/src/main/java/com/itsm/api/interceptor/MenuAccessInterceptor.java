package com.itsm.api.interceptor;

import com.itsm.api.service.MenuCacheService;
import com.itsm.core.domain.user.Menu;
import com.itsm.core.domain.user.MenuAccessLog;
import com.itsm.core.repository.user.MenuAccessLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuAccessInterceptor implements HandlerInterceptor {

    private final MenuCacheService menuCacheService;
    private final MenuAccessLogRepository menuAccessLogRepository;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${security.trusted-proxies:127.0.0.1,::1}")
    private List<String> trustedProxies;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        String requestUri = request.getRequestURI();

        List<Menu> allMenus = menuCacheService.getAllMenus();
        Menu matchedMenu = allMenus.stream()
                .filter(menu -> menu.getMenuUrl() != null && pathMatcher.match(menu.getMenuUrl(), requestUri))
                .findFirst()
                .orElse(null);

        if (matchedMenu == null) {
            return;
        }

        try {
            Long userId = (Long) authentication.getPrincipal();
            String roleCd = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                    .collect(Collectors.joining(","));

            String ipAddress = getClientIpAddress(request);

            MenuAccessLog accessLog = MenuAccessLog.builder()
                    .userId(userId)
                    .menuId(matchedMenu.getMenuId())
                    .roleCd(roleCd)
                    .ipAddress(ipAddress)
                    .build();

            menuAccessLogRepository.save(accessLog);
        } catch (Exception e) {
            log.warn("Failed to save menu access log for URI: {}", requestUri, e);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            String remoteAddr = request.getRemoteAddr();
            if (isTrustedProxy(remoteAddr)) {
                String[] ips = xForwardedFor.split(",");
                // 오른쪽에서 왼쪽으로 신뢰할 수 있는 프록시를 건너뛰고 첫 번째 비신뢰 IP 반환
                for (int i = ips.length - 1; i >= 0; i--) {
                    String ip = ips[i].trim();
                    if (!isTrustedProxy(ip)) {
                        return ip;
                    }
                }
                return ips[0].trim();
            }
            // remoteAddr이 신뢰할 수 없는 프록시라면 X-Forwarded-For를 무시
            log.warn("Untrusted proxy {} attempted X-Forwarded-For spoofing: {}", remoteAddr, xForwardedFor);
            return remoteAddr;
        }
        return request.getRemoteAddr();
    }

    private boolean isTrustedProxy(String ip) {
        if (ip == null) {
            return false;
        }
        for (String trusted : trustedProxies) {
            if (trusted.contains("/")) {
                // CIDR 표기법 지원 (예: 10.0.0.0/8)
                if (isInCidrRange(ip, trusted)) {
                    return true;
                }
            } else if (trusted.equals(ip)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInCidrRange(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            InetAddress network = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] networkBytes = network.getAddress();
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
