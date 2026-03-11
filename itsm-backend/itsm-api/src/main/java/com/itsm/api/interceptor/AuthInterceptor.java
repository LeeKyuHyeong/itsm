package com.itsm.api.interceptor;

import com.itsm.core.domain.user.Menu;
import com.itsm.core.domain.user.RoleMenu;
import com.itsm.core.domain.user.UserRole;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.user.MenuRepository;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final RoleMenuRepository roleMenuRepository;
    private final MenuRepository menuRepository;
    private final UserRoleRepository userRoleRepository;

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

        List<Menu> allMenus = menuRepository.findAll();
        Menu matchedMenu = allMenus.stream()
                .filter(menu -> menu.getMenuUrl() != null && pathMatcher.match(menu.getMenuUrl(), requestUri))
                .findFirst()
                .orElse(null);

        if (matchedMenu == null) {
            return true;
        }

        Long userId = (Long) authentication.getPrincipal();
        List<UserRole> userRoles = userRoleRepository.findByUserIdWithRole(userId);

        for (UserRole userRole : userRoles) {
            List<RoleMenu> roleMenus = roleMenuRepository.findByRoleId(userRole.getRoleId());
            for (RoleMenu roleMenu : roleMenus) {
                if (roleMenu.getMenuId().equals(matchedMenu.getMenuId())
                        && "Y".equals(roleMenu.getCanRead())) {
                    return true;
                }
            }
        }

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
