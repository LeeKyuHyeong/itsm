package com.itsm.api.interceptor;

import com.itsm.api.service.MenuCacheService;
import com.itsm.core.domain.user.Menu;
import com.itsm.core.domain.user.MenuAccessLog;
import com.itsm.core.repository.user.MenuAccessLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class MenuAccessInterceptorTest {

    @Mock
    private MenuCacheService menuCacheService;

    @Mock
    private MenuAccessLogRepository menuAccessLogRepository;

    @InjectMocks
    private MenuAccessInterceptor menuAccessInterceptor;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private Object handler;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handler = new Object();
        SecurityContextHolder.clearContext();
        ReflectionTestUtils.setField(menuAccessInterceptor, "trustedProxies", List.of("127.0.0.1", "::1"));
    }

    @Test
    @DisplayName("메뉴에 매칭되는 URL 접근 시 접근 로그를 저장한다")
    void shouldSaveAccessLogWhenMenuMatches() {
        // given
        setAuthentication(1L, List.of("ROLE_ADMIN"));
        given(request.getRequestURI()).willReturn("/api/v1/users");
        given(request.getRemoteAddr()).willReturn("127.0.0.1");
        given(request.getHeader("X-Forwarded-For")).willReturn(null);

        Menu menu = createMenu(10L, "사용자 관리", "/api/v1/users/**");
        given(menuCacheService.getAllMenus()).willReturn(List.of(menu));

        // when
        menuAccessInterceptor.postHandle(request, response, handler, null);

        // then
        ArgumentCaptor<MenuAccessLog> captor = ArgumentCaptor.forClass(MenuAccessLog.class);
        verify(menuAccessLogRepository).save(captor.capture());

        MenuAccessLog savedLog = captor.getValue();
        assertThat(savedLog.getUserId()).isEqualTo(1L);
        assertThat(savedLog.getMenuId()).isEqualTo(10L);
        assertThat(savedLog.getRoleCd()).isEqualTo("ADMIN");
        assertThat(savedLog.getIpAddress()).isEqualTo("127.0.0.1");
    }

    @Test
    @DisplayName("메뉴에 매칭되지 않는 URL은 로그를 저장하지 않는다")
    void shouldNotSaveLogWhenNoMenuMatches() {
        // given
        setAuthentication(1L, List.of("ROLE_ADMIN"));
        given(request.getRequestURI()).willReturn("/api/v1/some-other-endpoint");
        given(menuCacheService.getAllMenus()).willReturn(List.of());

        // when
        menuAccessInterceptor.postHandle(request, response, handler, null);

        // then
        verify(menuAccessLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("인증되지 않은 요청은 로그를 저장하지 않는다")
    void shouldNotSaveLogWhenNotAuthenticated() {
        // given - no authentication set

        // when
        menuAccessInterceptor.postHandle(request, response, handler, null);

        // then
        verify(menuAccessLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("신뢰할 수 있는 프록시를 통한 X-Forwarded-For에서 클라이언트 IP를 추출한다")
    void shouldExtractClientIpFromTrustedProxy() {
        // given
        setAuthentication(1L, List.of("ROLE_USER"));
        given(request.getRequestURI()).willReturn("/api/v1/users");
        given(request.getHeader("X-Forwarded-For")).willReturn("192.168.1.100, 127.0.0.1");
        given(request.getRemoteAddr()).willReturn("127.0.0.1");

        Menu menu = createMenu(10L, "사용자 관리", "/api/v1/users/**");
        given(menuCacheService.getAllMenus()).willReturn(List.of(menu));

        // when
        menuAccessInterceptor.postHandle(request, response, handler, null);

        // then
        ArgumentCaptor<MenuAccessLog> captor = ArgumentCaptor.forClass(MenuAccessLog.class);
        verify(menuAccessLogRepository).save(captor.capture());
        assertThat(captor.getValue().getIpAddress()).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("신뢰할 수 없는 프록시의 X-Forwarded-For는 무시하고 remoteAddr을 사용한다")
    void shouldIgnoreXForwardedForFromUntrustedProxy() {
        // given
        setAuthentication(1L, List.of("ROLE_USER"));
        given(request.getRequestURI()).willReturn("/api/v1/users");
        given(request.getHeader("X-Forwarded-For")).willReturn("10.0.0.1");
        given(request.getRemoteAddr()).willReturn("203.0.113.50"); // 신뢰할 수 없는 프록시

        Menu menu = createMenu(10L, "사용자 관리", "/api/v1/users/**");
        given(menuCacheService.getAllMenus()).willReturn(List.of(menu));

        // when
        menuAccessInterceptor.postHandle(request, response, handler, null);

        // then
        ArgumentCaptor<MenuAccessLog> captor = ArgumentCaptor.forClass(MenuAccessLog.class);
        verify(menuAccessLogRepository).save(captor.capture());
        assertThat(captor.getValue().getIpAddress()).isEqualTo("203.0.113.50");
    }

    @Test
    @DisplayName("X-Forwarded-For가 없으면 remoteAddr을 사용한다")
    void shouldFallbackToRemoteAddr() {
        // given
        setAuthentication(1L, List.of("ROLE_USER"));
        given(request.getRequestURI()).willReturn("/api/v1/users");
        given(request.getHeader("X-Forwarded-For")).willReturn(null);
        given(request.getRemoteAddr()).willReturn("10.0.0.5");

        Menu menu = createMenu(10L, "사용자 관리", "/api/v1/users/**");
        given(menuCacheService.getAllMenus()).willReturn(List.of(menu));

        // when
        menuAccessInterceptor.postHandle(request, response, handler, null);

        // then
        ArgumentCaptor<MenuAccessLog> captor = ArgumentCaptor.forClass(MenuAccessLog.class);
        verify(menuAccessLogRepository).save(captor.capture());
        assertThat(captor.getValue().getIpAddress()).isEqualTo("10.0.0.5");
    }

    @Test
    @DisplayName("CIDR 범위의 신뢰할 수 있는 프록시도 인식한다")
    void shouldRecognizeTrustedProxiesInCidrRange() {
        // given
        ReflectionTestUtils.setField(menuAccessInterceptor, "trustedProxies", List.of("10.0.0.0/8"));
        setAuthentication(1L, List.of("ROLE_USER"));
        given(request.getRequestURI()).willReturn("/api/v1/users");
        given(request.getHeader("X-Forwarded-For")).willReturn("192.168.1.100");
        given(request.getRemoteAddr()).willReturn("10.0.0.50"); // 10.0.0.0/8 범위 내

        Menu menu = createMenu(10L, "사용자 관리", "/api/v1/users/**");
        given(menuCacheService.getAllMenus()).willReturn(List.of(menu));

        // when
        menuAccessInterceptor.postHandle(request, response, handler, null);

        // then
        ArgumentCaptor<MenuAccessLog> captor = ArgumentCaptor.forClass(MenuAccessLog.class);
        verify(menuAccessLogRepository).save(captor.capture());
        assertThat(captor.getValue().getIpAddress()).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("MenuCacheService를 통해 메뉴를 조회한다")
    void shouldUseMenuCacheService() {
        // given
        setAuthentication(1L, List.of("ROLE_USER"));
        given(request.getRequestURI()).willReturn("/api/v1/some-endpoint");
        given(menuCacheService.getAllMenus()).willReturn(List.of());

        // when
        menuAccessInterceptor.postHandle(request, response, handler, null);

        // then
        verify(menuCacheService).getAllMenus();
    }

    private Menu createMenu(Long menuId, String menuNm, String menuUrl) {
        Menu menu = Menu.builder()
                .menuNm(menuNm)
                .menuUrl(menuUrl)
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(menu, "menuId", menuId);
        return menu;
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
