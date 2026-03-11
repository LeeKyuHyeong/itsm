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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthInterceptorTest {

    @Mock
    private RoleMenuRepository roleMenuRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private Object handler;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handler = new Object();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증 엔드포인트는 인터셉터를 건너뛴다")
    void shouldSkipAuthEndpoints() {
        // given
        given(request.getRequestURI()).willReturn("/api/v1/auth/login");

        // when
        boolean result = authInterceptor.preHandle(request, response, handler);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Swagger 엔드포인트는 인터셉터를 건너뛴다")
    void shouldSkipSwaggerEndpoints() {
        // given
        given(request.getRequestURI()).willReturn("/v3/api-docs/swagger-config");

        // when
        boolean result = authInterceptor.preHandle(request, response, handler);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SUPER_ADMIN 역할은 모든 접근이 허용된다")
    void shouldAllowSuperAdminAccess() {
        // given
        given(request.getRequestURI()).willReturn("/api/v1/users");
        setAuthentication(1L, List.of("ROLE_SUPER_ADMIN"));

        // when
        boolean result = authInterceptor.preHandle(request, response, handler);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("메뉴와 매칭되지 않는 URL은 접근이 허용된다")
    void shouldAllowAccessWhenNoMenuMatches() {
        // given
        given(request.getRequestURI()).willReturn("/api/v1/some-endpoint");
        setAuthentication(1L, List.of("ROLE_USER"));
        given(menuRepository.findAll()).willReturn(List.of());

        // when
        boolean result = authInterceptor.preHandle(request, response, handler);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("권한이 있는 사용자는 메뉴 접근이 허용된다")
    void shouldAllowAccessWhenUserHasPermission() {
        // given
        given(request.getRequestURI()).willReturn("/api/v1/users");
        setAuthentication(1L, List.of("ROLE_ADMIN"));

        Menu menu = Menu.builder()
                .menuNm("사용자 관리")
                .menuUrl("/api/v1/users/**")
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(menu, "menuId", 10L);
        given(menuRepository.findAll()).willReturn(List.of(menu));

        UserRole userRole = new UserRole(1L, 5L, 1L);
        given(userRoleRepository.findByUserIdWithRole(1L)).willReturn(List.of(userRole));

        RoleMenu roleMenu = new RoleMenu(5L, 10L, "Y", "N", 1L);
        given(roleMenuRepository.findByRoleId(5L)).willReturn(List.of(roleMenu));

        // when
        boolean result = authInterceptor.preHandle(request, response, handler);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("권한이 없는 사용자는 ACCESS_DENIED 예외가 발생한다")
    void shouldThrowAccessDeniedWhenNoPermission() {
        // given
        given(request.getRequestURI()).willReturn("/api/v1/users");
        setAuthentication(1L, List.of("ROLE_USER"));

        Menu menu = Menu.builder()
                .menuNm("사용자 관리")
                .menuUrl("/api/v1/users/**")
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(menu, "menuId", 10L);
        given(menuRepository.findAll()).willReturn(List.of(menu));

        UserRole userRole = new UserRole(1L, 5L, 1L);
        given(userRoleRepository.findByUserIdWithRole(1L)).willReturn(List.of(userRole));

        RoleMenu roleMenu = new RoleMenu(5L, 10L, "N", "N", 1L);
        given(roleMenuRepository.findByRoleId(5L)).willReturn(List.of(roleMenu));

        // when & then
        assertThatThrownBy(() -> authInterceptor.preHandle(request, response, handler))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    @DisplayName("역할에 해당하는 메뉴 권한이 없으면 ACCESS_DENIED 예외가 발생한다")
    void shouldThrowAccessDeniedWhenNoRoleMenuExists() {
        // given
        given(request.getRequestURI()).willReturn("/api/v1/users");
        setAuthentication(1L, List.of("ROLE_USER"));

        Menu menu = Menu.builder()
                .menuNm("사용자 관리")
                .menuUrl("/api/v1/users/**")
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(menu, "menuId", 10L);
        given(menuRepository.findAll()).willReturn(List.of(menu));

        UserRole userRole = new UserRole(1L, 5L, 1L);
        given(userRoleRepository.findByUserIdWithRole(1L)).willReturn(List.of(userRole));

        given(roleMenuRepository.findByRoleId(5L)).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> authInterceptor.preHandle(request, response, handler))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    private void setAuthentication(Long userId, List<String> roles) {
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, "testUser", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
