package com.itsm.api.interceptor;

import com.itsm.api.security.ClientIpResolver;
import com.itsm.api.service.MenuCacheService;
import com.itsm.core.domain.user.Menu;
import com.itsm.core.domain.user.MenuAccessLog;
import com.itsm.core.repository.user.MenuAccessLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 메뉴 접근 로그(tb_menu_access_log).
 * 2026-09-16 P1 이전에는 menu_url(프론트 라우트) 과 API URI 를 대조해 한 번도 저장되지 않았다.
 * 판정은 {@link ApiMenuMapper}(권한 대상 요청만 기록), IP 는 {@link ClientIpResolver}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuAccessInterceptor implements HandlerInterceptor {

    private final MenuCacheService menuCacheService;
    private final MenuAccessLogRepository menuAccessLogRepository;
    private final ApiMenuMapper apiMenuMapper;
    private final ClientIpResolver clientIpResolver;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        String requestUri = request.getRequestURI();
        Optional<String> menuUrl = apiMenuMapper.resolveMenuUrl(requestUri, request.getMethod());
        if (menuUrl.isEmpty()) {
            return;
        }

        Menu matchedMenu = menuCacheService.getAllMenus().stream()
                .filter(menu -> menuUrl.get().equals(menu.getMenuUrl()))
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

            MenuAccessLog accessLog = MenuAccessLog.builder()
                    .userId(userId)
                    .menuId(matchedMenu.getMenuId())
                    .roleCd(roleCd)
                    .ipAddress(clientIpResolver.resolve(request))
                    .build();
            menuAccessLogRepository.save(accessLog);
        } catch (Exception e) {
            log.warn("Failed to save menu access log for URI: {}", requestUri, e);
        }
    }
}
