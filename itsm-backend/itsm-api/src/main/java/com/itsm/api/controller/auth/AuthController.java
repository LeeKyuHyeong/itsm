package com.itsm.api.controller.auth;

import com.itsm.api.dto.auth.*;
import com.itsm.api.service.auth.AuthService;
import com.itsm.core.dto.ApiResponse;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7 days

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest httpRequest,
                                            HttpServletResponse httpResponse) {
        String ipAddress = httpRequest.getRemoteAddr();
        LoginResponse loginResponse = authService.login(request, ipAddress);

        addRefreshTokenCookie(httpResponse, loginResponse.getRefreshToken());

        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(HttpServletRequest request,
                                              HttpServletResponse httpResponse) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "리프레시 토큰이 없습니다.");
        }

        LoginResponse loginResponse = authService.refresh(refreshToken);

        addRefreshTokenCookie(httpResponse, loginResponse.getRefreshToken());

        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        Long userId = (Long) authentication.getPrincipal();
        String ipAddress = request.getRemoteAddr();

        authService.logout(userId, ipAddress);

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

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
