package com.itsm.api.service.auth;

import com.itsm.api.dto.auth.*;
import com.itsm.api.security.JwtTokenProvider;
import com.itsm.core.domain.user.AccessLog;
import com.itsm.core.domain.user.User;
import com.itsm.core.domain.user.UserRole;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.user.AccessLogRepository;
import com.itsm.core.repository.user.UserRepository;
import com.itsm.core.repository.user.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AccessLogRepository accessLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final int AUTO_UNLOCK_MINUTES = 30;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$"
    );

    public LoginResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> {
                    logAccess(null, request.getLoginId(), "LOGIN", ipAddress, false, "사용자를 찾을 수 없습니다.");
                    return new BusinessException(ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
                });

        // Check account status
        if (!"ACTIVE".equals(user.getStatus()) && !"LOCKED".equals(user.getStatus())) {
            logAccess(user.getUserId(), user.getLoginId(), "LOGIN", ipAddress, false, "비활성 계정");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "비활성 계정입니다.");
        }

        // Check locked account with auto-unlock
        if (user.isLocked()) {
            if (user.getLastLoginAt() != null &&
                    user.getLastLoginAt().plusMinutes(AUTO_UNLOCK_MINUTES).isBefore(LocalDateTime.now())) {
                user.unlock();
            } else {
                logAccess(user.getUserId(), user.getLoginId(), "LOGIN", ipAddress, false, "계정 잠금");
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
            }
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.recordLoginFailure();
            if (user.getLoginFailCnt() >= MAX_LOGIN_FAIL_COUNT) {
                user.lock();
                logAccess(user.getUserId(), user.getLoginId(), "LOGIN", ipAddress, false, "비밀번호 오류 - 계정 잠금");
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED, "로그인 실패 횟수 초과로 계정이 잠겼습니다.");
            }
            logAccess(user.getUserId(), user.getLoginId(), "LOGIN", ipAddress, false, "비밀번호 불일치");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // Success
        user.recordLoginSuccess();

        List<String> roles = getUserRoles(user.getUserId());

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getLoginId(), roles);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        logAccess(user.getUserId(), user.getLoginId(), "LOGIN", ipAddress, true, null);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .userNm(user.getUserNm())
                .roles(roles)
                .build();
    }

    public LoginResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        List<String> roles = getUserRoles(userId);

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getLoginId(), roles);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .userNm(user.getUserNm())
                .roles(roles)
                .build();
    }

    public void logout(Long userId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));
        logAccess(userId, user.getLoginId(), "LOGOUT", ipAddress, true, null);
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        List<String> roles = getUserRoles(userId);

        String deptName = null;
        String companyName = null;
        if (user.getDepartment() != null) {
            deptName = user.getDepartment().getDeptNm();
            if (user.getDepartment().getCompany() != null) {
                companyName = user.getDepartment().getCompany().getCompanyNm();
            }
        }

        boolean mustChangePassword = user.getPwdChangedAt() == null ||
                user.getPwdChangedAt().plusDays(90).isBefore(LocalDateTime.now());

        return UserInfoResponse.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .userNm(user.getUserNm())
                .email(user.getEmail())
                .roles(roles)
                .deptName(deptName)
                .companyName(companyName)
                .pwdChangedAt(user.getPwdChangedAt())
                .mustChangePassword(mustChangePassword)
                .build();
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "현재 비밀번호가 올바르지 않습니다.");
        }

        if (!PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "비밀번호는 8자 이상이며 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다.");
        }

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    private List<String> getUserRoles(Long userId) {
        return userRoleRepository.findByUserIdWithRole(userId).stream()
                .map(UserRole::getRole)
                .map(role -> role.getRoleCd())
                .toList();
    }

    private void logAccess(Long userId, String loginId, String actionType,
                           String ipAddress, boolean success, String failReason) {
        AccessLog log = AccessLog.builder()
                .userId(userId)
                .loginId(loginId)
                .actionType(actionType)
                .ipAddress(ipAddress)
                .successYn(success ? "Y" : "N")
                .failReason(failReason)
                .build();
        accessLogRepository.save(log);
    }
}
