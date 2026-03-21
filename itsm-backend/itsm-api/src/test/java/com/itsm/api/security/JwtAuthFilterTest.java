package com.itsm.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class JwtAuthFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Authorization 헤더의 Bearer 토큰으로 인증한다")
    void authenticatesFromAuthorizationHeader() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/users");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.validateToken("valid-token")).willReturn(true);
        given(jwtTokenProvider.getUserId("valid-token")).willReturn(1L);
        given(jwtTokenProvider.getLoginId("valid-token")).willReturn("admin");
        given(jwtTokenProvider.getRoles("valid-token")).willReturn(List.of("ADMIN"));

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(1L);
    }

    @Test
    @DisplayName("accessToken 쿠키로 인증한다")
    void authenticatesFromAccessTokenCookie() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/users");
        request.setCookies(new Cookie("accessToken", "cookie-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.validateToken("cookie-token")).willReturn(true);
        given(jwtTokenProvider.getUserId("cookie-token")).willReturn(2L);
        given(jwtTokenProvider.getLoginId("cookie-token")).willReturn("user1");
        given(jwtTokenProvider.getRoles("cookie-token")).willReturn(List.of("USER"));

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Authorization 헤더가 쿠키보다 우선한다")
    void authorizationHeaderTakesPrecedenceOverCookie() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/users");
        request.addHeader("Authorization", "Bearer header-token");
        request.setCookies(new Cookie("accessToken", "cookie-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.validateToken("header-token")).willReturn(true);
        given(jwtTokenProvider.getUserId("header-token")).willReturn(1L);
        given(jwtTokenProvider.getLoginId("header-token")).willReturn("admin");
        given(jwtTokenProvider.getRoles("header-token")).willReturn(List.of("ADMIN"));

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(1L);
    }

    @Test
    @DisplayName("토큰이 없으면 인증 컨텍스트가 설정되지 않는다")
    void noTokenMeansNoAuthentication() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
