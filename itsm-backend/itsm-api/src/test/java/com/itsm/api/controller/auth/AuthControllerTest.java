package com.itsm.api.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.auth.*;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.security.JwtTokenProvider;
import com.itsm.api.security.LoginRateLimiter;
import com.itsm.api.service.auth.AuthService;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private LoginRateLimiter loginRateLimiter;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /login - 로그인 성공 시 200과 사용자 정보를 반환하고 accessToken 쿠키를 설정한다")
    void login_success_returns200WithCookie() throws Exception {
        // given
        LoginRequest request = new LoginRequest("admin", "Password1!");
        LoginResponse response = LoginResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .userId(1L)
                .loginId("admin")
                .userNm("관리자")
                .roles(List.of("ADMIN"))
                .build();

        given(authService.login(any(LoginRequest.class), anyString())).willReturn(response);
        given(jwtTokenProvider.getAccessTokenExpirySeconds()).willReturn(3600L);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").doesNotExist())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.loginId").value("admin"))
                .andExpect(jsonPath("$.data.userNm").value("관리자"))
                .andExpect(jsonPath("$.data.roles[0]").value("ADMIN"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("POST /login - 잘못된 자격 증명 시 401을 반환한다")
    void login_invalidCredentials_returns401() throws Exception {
        // given
        LoginRequest request = new LoginRequest("admin", "wrongPassword");
        given(authService.login(any(LoginRequest.class), anyString()))
                .willThrow(new BusinessException(ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("E401_001"));
    }

    @Test
    @DisplayName("GET /me - 인증된 사용자의 정보를 반환한다")
    void getMe_returnsUserInfo() throws Exception {
        // given
        setAuthentication(1L, "admin");

        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userId(1L)
                .loginId("admin")
                .userNm("관리자")
                .email("admin@test.com")
                .roles(List.of("ADMIN"))
                .deptName("IT부서")
                .companyName("테스트회사")
                .pwdChangedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .mustChangePassword(false)
                .build();

        given(authService.getMe(1L)).willReturn(userInfo);

        // when & then
        mockMvc.perform(get("/api/v1/auth/me")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.loginId").value("admin"))
                .andExpect(jsonPath("$.data.userNm").value("관리자"))
                .andExpect(jsonPath("$.data.email").value("admin@test.com"))
                .andExpect(jsonPath("$.data.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.data.deptName").value("IT부서"))
                .andExpect(jsonPath("$.data.companyName").value("테스트회사"));
    }

    @Test
    @DisplayName("PATCH /password - 비밀번호 변경 성공 시 200을 반환한다")
    void changePassword_success_returns200() throws Exception {
        // given
        setAuthentication(1L, "admin");

        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword1!", "NewPassword1!");
        doNothing().when(authService).changePassword(any(Long.class), any(ChangePasswordRequest.class));

        // when & then
        mockMvc.perform(patch("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private void setAuthentication(Long userId, String loginId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId, loginId,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("POST /login - Rate Limit 초과 시 429를 반환한다")
    void login_rateLimited_returns429() throws Exception {
        LoginRequest request = new LoginRequest("admin", "Password1!");
        given(loginRateLimiter.isBlocked(anyString())).willReturn(true);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false));

        verify(authService, never()).login(any(LoginRequest.class), anyString());
    }

    @Test
    @DisplayName("POST /refresh - refreshToken 쿠키로 새 토큰을 발급한다")
    void refresh_success_returns200() throws Exception {
        LoginResponse response = LoginResponse.builder()
                .accessToken("new-access")
                .refreshToken("new-refresh")
                .userId(1L)
                .loginId("admin")
                .userNm("관리자")
                .roles(List.of("ADMIN"))
                .build();
        given(authService.refresh("valid-refresh")).willReturn(response);
        given(jwtTokenProvider.getAccessTokenExpirySeconds()).willReturn(3600L);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refreshToken", "valid-refresh")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").doesNotExist())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("POST /refresh - 쿠키가 없으면 INVALID_TOKEN 예외를 반환한다")
    void refresh_noCookie_returnsInvalidToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("E401_002"));
    }

    @Test
    @DisplayName("POST /logout - 로그아웃 시 토큰이 블랙리스트되고 쿠키가 만료된다")
    void logout_success_invalidatesTokensAndClearsCookies() throws Exception {
        setAuthentication(1L, "admin");
        doNothing().when(authService).logout(eq(1L), anyString());
        doNothing().when(jwtTokenProvider).invalidate(anyString());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new Cookie("accessToken", "access-token"),
                                new Cookie("refreshToken", "refresh-token"))
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(header().exists("Set-Cookie"));

        verify(jwtTokenProvider).invalidate("access-token");
        verify(jwtTokenProvider).invalidate("refresh-token");
        verify(authService).logout(eq(1L), anyString());
    }
}
