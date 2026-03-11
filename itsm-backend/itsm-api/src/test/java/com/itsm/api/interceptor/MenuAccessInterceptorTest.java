package com.itsm.api.interceptor;

import com.itsm.core.domain.user.Menu;
import com.itsm.core.domain.user.MenuAccessLog;
import com.itsm.core.repository.user.MenuAccessLogRepository;
import com.itsm.core.repository.user.MenuRepository;
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
    private MenuRepository menuRepository;

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
    }

    @Test
    @DisplayName("메뉴에 매칭되는 URL 접근 시 접근 로그를 저장한다")
    void shouldSaveAccessLogWhenMenuMatches() {
        // given
        setAuthentication(1L, List.of("ROLE_ADMIN"));
        given(request.getRequestURI()).willReturn("/api/v1/users");
        given(request.getRemoteAddr()).willReturn("127.0.0.1");
        given(request.getHeader("X-Forwarded-For")).willReturn(null);

        Menu menu = Menu.builder()
                .menuNm("사용자 관리")
                .menuUrl("/api/v1/users/**")
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(menu, "menuId", 10L);
        given(menuRepository.findAll()).willReturn(List.of(menu));

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
        given(menuRepository.findAll()).willReturn(List.of());

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
    @DisplayName("X-Forwarded-For 헤더가 있으면 해당 IP를 사용한다")
    void shouldUseXForwardedForHeader() {
        // given
        setAuthentication(1L, List.of("ROLE_USER"));
        given(request.getRequestURI()).willReturn("/api/v1/users");
        given(request.getHeader("X-Forwarded-For")).willReturn("192.168.1.100, 10.0.0.1");

        Menu menu = Menu.builder()
                .menuNm("사용자 관리")
                .menuUrl("/api/v1/users/**")
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(menu, "menuId", 10L);
        given(menuRepository.findAll()).willReturn(List.of(menu));

        // when
        menuAccessInterceptor.postHandle(request, response, handler, null);

        // then
        ArgumentCaptor<MenuAccessLog> captor = ArgumentCaptor.forClass(MenuAccessLog.class);
        verify(menuAccessLogRepository).save(captor.capture());
        assertThat(captor.getValue().getIpAddress()).isEqualTo("192.168.1.100");
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
