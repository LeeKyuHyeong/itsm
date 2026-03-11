package com.itsm.api.interceptor;

import com.itsm.core.domain.user.Menu;
import com.itsm.core.domain.user.MenuAccessLog;
import com.itsm.core.repository.user.MenuAccessLogRepository;
import com.itsm.core.repository.user.MenuRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuAccessInterceptor implements HandlerInterceptor {

    private final MenuRepository menuRepository;
    private final MenuAccessLogRepository menuAccessLogRepository;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        String requestUri = request.getRequestURI();

        List<Menu> allMenus = menuRepository.findAll();
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
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
