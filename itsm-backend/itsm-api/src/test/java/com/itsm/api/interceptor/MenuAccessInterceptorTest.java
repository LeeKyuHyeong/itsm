package com.itsm.api.interceptor;

import com.itsm.api.security.ClientIpResolver;
import com.itsm.api.service.MenuCacheService;
import com.itsm.core.domain.user.Menu;
import com.itsm.core.domain.user.MenuAccessLog;
import com.itsm.core.repository.user.MenuAccessLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 2026-09-16 전수조사 P1 — 메뉴 접근 로그(tb_menu_access_log)는 menu_url(프론트 라우트) 과 API URI 를 대조해
 * 한 번도 저장되지 않았다. ApiMenuMapper 로 판정하고, IP 는 공통 ClientIpResolver 로 얻는다.
 */
@ExtendWith(MockitoExtension.class)
class MenuAccessInterceptorTest {

    @Mock
    private MenuCacheService menuCacheService;

    @Mock
    private MenuAccessLogRepository menuAccessLogRepository;

    private MenuAccessInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private final Object handler = new Object();

    @BeforeEach
    void setUp() {
        interceptor = new MenuAccessInterceptor(menuCacheService, menuAccessLogRepository,
                new ApiMenuMapper(), new ClientIpResolver(List.of("127.0.0.1", "::1", "172.16.0.0/12")));
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(Long userId, List<String> roles) {
        List<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, "testUser", authorities));
    }

    private Menu menu(Long id, String url) {
        Menu menu = Menu.builder().menuNm("m" + id).menuUrl(url).sortOrder(1).build();
        ReflectionTestUtils.setField(menu, "menuId", id);
        return menu;
    }

    @Test
    @DisplayName("GET /api/v1/incidents 는 메뉴 /incidents 접근으로 기록된다 (실제 시드 URL)")
    void savesLogForMappedRequest() {
        setAuthentication(1L, List.of("ROLE_PM"));
        given(request.getRequestURI()).willReturn("/api/v1/incidents");
        given(request.getMethod()).willReturn("GET");
        given(request.getRemoteAddr()).willReturn("172.19.0.3");
        given(request.getHeader("X-Forwarded-For")).willReturn("203.0.113.7, 172.19.0.1");
        given(menuCacheService.getAllMenus()).willReturn(List.of(menu(11L, "/incidents"), menu(16L, "/changes")));

        interceptor.postHandle(request, response, handler, null);

        ArgumentCaptor<MenuAccessLog> captor = ArgumentCaptor.forClass(MenuAccessLog.class);
        verify(menuAccessLogRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getMenuId()).isEqualTo(11L);
        assertThat(captor.getValue().getRoleCd()).isEqualTo("PM");
        assertThat(captor.getValue().getIpAddress()).as("프록시 뒤 실제 클라이언트 IP").isEqualTo("203.0.113.7");
    }

    @Test
    @DisplayName("담당자 선택용 GET /api/v1/users 는 계정관리 접근으로 기록하지 않는다")
    void exemptRead_notLogged() {
        setAuthentication(1L, List.of("ROLE_PM"));
        given(request.getRequestURI()).willReturn("/api/v1/users");
        given(request.getMethod()).willReturn("GET");

        interceptor.postHandle(request, response, handler, null);

        verify(menuAccessLogRepository, never()).save(any());
        verify(menuCacheService, never()).getAllMenus();
    }

    @Test
    @DisplayName("매핑에 없는 URL 은 기록하지 않는다")
    void unmapped_notLogged() {
        setAuthentication(1L, List.of("ROLE_PM"));
        given(request.getRequestURI()).willReturn("/api/v1/notifications");
        given(request.getMethod()).willReturn("GET");

        interceptor.postHandle(request, response, handler, null);

        verify(menuAccessLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("인증되지 않은 요청은 기록하지 않는다")
    void unauthenticated_notLogged() {
        interceptor.postHandle(request, response, handler, null);

        verify(menuAccessLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("저장 실패는 요청을 깨뜨리지 않는다")
    void saveFailure_swallowed() {
        setAuthentication(1L, List.of("ROLE_PM"));
        given(request.getRequestURI()).willReturn("/api/v1/incidents");
        given(request.getMethod()).willReturn("GET");
        given(request.getRemoteAddr()).willReturn("127.0.0.1");
        given(menuCacheService.getAllMenus()).willReturn(List.of(menu(11L, "/incidents")));
        given(menuAccessLogRepository.save(any())).willThrow(new RuntimeException("db down"));

        interceptor.postHandle(request, response, handler, null);
        // 예외가 밖으로 나오지 않으면 통과
    }
}
