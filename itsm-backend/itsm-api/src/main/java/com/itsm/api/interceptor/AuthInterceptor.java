package com.itsm.api.interceptor;

import com.itsm.api.service.MenuCacheService;
import com.itsm.core.domain.user.Menu;
import com.itsm.core.domain.user.RoleMenu;
import com.itsm.core.domain.user.UserRole;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.user.RoleMenuRepository;
import com.itsm.core.repository.user.UserRoleRepository;
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

import java.util.List;
import java.util.Optional;

/**
 * 메뉴 기반 인가 (tb_role_menu.can_read / can_write).
 * <p>
 * 2026-09-16 전수조사 P1 이전: menu_url(프론트 라우트) 과 API URI 를 직접 대조해 매칭이 0 → matchedMenu null → 무조건 통과.
 * 즉 "3중 RBAC" 의 이 층은 통과 전용이었고, 감사자(읽기 전용)도 장애를 생성·수정할 수 있었다.
 * 지금은 {@link ApiMenuMapper} 가 URI+메서드를 메뉴 URL 로 바꾸고, GET 계열은 can_read, 나머지는 can_write 를 요구한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final RoleMenuRepository roleMenuRepository;
    private final MenuCacheService menuCacheService;
    private final UserRoleRepository userRoleRepository;
    private final ApiMenuMapper apiMenuMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String[] SKIP_PATTERNS = {
            "/api/v1/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestUri = request.getRequestURI();

        if (shouldSkip(requestUri)) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_SUPER_ADMIN"::equals);
        if (isSuperAdmin) {
            return true;
        }

        String method = request.getMethod();
        Optional<String> menuUrl = apiMenuMapper.resolveMenuUrl(requestUri, method);
        if (menuUrl.isEmpty()) {
            return true; // 메뉴 권한 대상이 아닌 요청 (알림, 게시판, 공통 조회 등)
        }

        Menu matchedMenu = menuCacheService.getAllMenus().stream()
                .filter(menu -> menuUrl.get().equals(menu.getMenuUrl()))
                .findFirst()
                .orElse(null);
        if (matchedMenu == null) {
            // 관리자가 메뉴 행을 지웠거나 URL 을 바꾼 경우. 서비스를 막는 대신 통과시키되 반드시 흔적을 남긴다.
            log.warn("[AuthInterceptor] 매핑된 메뉴 {} 가 tb_menu 에 없어 권한 검사를 건너뜀: {} {}", menuUrl.get(), method, requestUri);
            return true;
        }

        boolean write = apiMenuMapper.isWrite(method);
        Long userId = (Long) authentication.getPrincipal();
        List<UserRole> userRoles = userRoleRepository.findByUserIdWithRole(userId);
        for (UserRole userRole : userRoles) {
            for (RoleMenu roleMenu : roleMenuRepository.findByRoleId(userRole.getRoleId())) {
                if (!roleMenu.getMenuId().equals(matchedMenu.getMenuId())) {
                    continue;
                }
                if (write ? "Y".equals(roleMenu.getCanWrite()) : "Y".equals(roleMenu.getCanRead())) {
                    return true;
                }
            }
        }

        log.info("[AuthInterceptor] 접근 거부: userId={} {} {} → 메뉴 {}({})", userId, method, requestUri,
                matchedMenu.getMenuNm(), write ? "can_write" : "can_read");
        throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    private boolean shouldSkip(String requestUri) {
        for (String pattern : SKIP_PATTERNS) {
            if (pathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }
        return false;
    }
}
