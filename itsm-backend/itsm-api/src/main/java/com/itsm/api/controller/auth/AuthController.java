package com.itsm.api.controller.auth;

import com.itsm.api.dto.auth.*;
import com.itsm.api.security.JwtTokenProvider;
import com.itsm.api.security.LoginRateLimiter;
import com.itsm.api.service.auth.AuthService;
import com.itsm.api.util.CookieUtils;
import com.itsm.core.dto.ApiResponse;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginRateLimiter loginRateLimiter;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7 days

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest httpRequest,
                                            HttpServletResponse httpResponse) {
        String ipAddress = httpRequest.getRemoteAddr();

        if (loginRateLimiter.isBlocked(ipAddress)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "로그인 시도가 너무 많습니다. 잠시 후 다시 시도해주세요.");
        }
        loginRateLimiter.recordAttempt(ipAddress);

        LoginResponse loginResponse = authService.login(request, ipAddress);

        addAccessTokenCookie(httpResponse, loginResponse.getAccessToken());
        addRefreshTokenCookie(httpResponse, loginResponse.getRefreshToken());

        LoginResponse cookieSafeResponse = loginResponse.withoutTokens();

        return ApiResponse.success(cookieSafeResponse);
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(HttpServletRequest request,
                                              HttpServletResponse httpResponse) {
        String refreshToken = CookieUtils.extractCookie(request, REFRESH_TOKEN_COOKIE);
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "리프레시 토큰이 없습니다.");
        }

        LoginResponse loginResponse = authService.refresh(refreshToken);

        addAccessTokenCookie(httpResponse, loginResponse.getAccessToken());
        addRefreshTokenCookie(httpResponse, loginResponse.getRefreshToken());

        LoginResponse cookieSafeResponse = loginResponse.withoutTokens();

        return ApiResponse.success(cookieSafeResponse);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        Long userId = (Long) authentication.getPrincipal();
        String ipAddress = request.getRemoteAddr();

        // Blacklist current access token
        String accessToken = CookieUtils.extractCookie(request, ACCESS_TOKEN_COOKIE);
        if (accessToken != null) {
            jwtTokenProvider.invalidate(accessToken);
        }

        // Blacklist refresh token
        String refreshToken = CookieUtils.extractCookie(request, REFRESH_TOKEN_COOKIE);
        if (refreshToken != null) {
            jwtTokenProvider.invalidate(refreshToken);
        }

        authService.logout(userId, ipAddress);

        clearAccessTokenCookie(response);
        clearRefreshTokenCookie(response);

        return ApiResponse.success();
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getMe(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserInfoResponse userInfo = authService.getMe(userId);
        return ApiResponse.success(userInfo);
    }

    @PatchMapping("/password")
    public ApiResponse<Void> changePassword(Authentication authentication,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        authService.changePassword(userId, request);
        return ApiResponse.success();
    }

    private void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, accessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api")
                .maxAge(jwtTokenProvider.getAccessTokenExpirySeconds())
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/v1/auth")
                .maxAge(REFRESH_TOKEN_MAX_AGE)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/v1/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}
