package com.itsm.api.service.auth;

import com.itsm.api.dto.auth.ChangePasswordRequest;
import com.itsm.api.dto.auth.LoginRequest;
import com.itsm.api.dto.auth.LoginResponse;
import com.itsm.api.dto.auth.UserInfoResponse;
import com.itsm.api.security.JwtTokenProvider;
import com.itsm.core.constant.UserStatus;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.company.Department;
import com.itsm.core.domain.user.Role;
import com.itsm.core.domain.user.User;
import com.itsm.core.domain.user.UserRole;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.user.AccessLogRepository;
import com.itsm.core.repository.user.UserRepository;
import com.itsm.core.repository.user.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private AccessLogRepository accessLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private UserRole userRole;
    private Role role;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .loginId("admin")
                .password("encodedPassword")
                .userNm("관리자")
                .status("ACTIVE")
                .build();
        ReflectionTestUtils.setField(activeUser, "userId", 1L);

        role = Role.builder()
                .roleNm("관리자")
                .roleCd("ADMIN")
                .build();
        ReflectionTestUtils.setField(role, "roleId", 1L);

        userRole = new UserRole(1L, 1L, 1L);
        ReflectionTestUtils.setField(userRole, "role", role);
    }

    @Test
    @DisplayName("로그인 성공 시 토큰과 사용자 정보를 반환한다")
    void login_success_returnsTokens() {
        // given
        LoginRequest request = new LoginRequest("admin", "password123");
        given(userRepository.findByLoginId("admin")).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
        given(userRoleRepository.findByUserIdWithRole(1L)).willReturn(List.of(userRole));
        given(jwtTokenProvider.createAccessToken(eq(1L), eq("admin"), anyList()))
                .willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(1L)).willReturn("refresh-token");
        given(accessLogRepository.save(any())).willReturn(null);

        // when
        LoginResponse response = authService.login(request, "127.0.0.1");

        // then
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getLoginId()).isEqualTo("admin");
        assertThat(response.getUserNm()).isEqualTo("관리자");
        assertThat(response.getRoles()).containsExactly("ADMIN");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외가 발생하고 실패 횟수가 증가한다")
    void login_wrongPassword_throwsExceptionAndIncrementsFailCount() {
        // given
        LoginRequest request = new LoginRequest("admin", "wrongPassword");
        given(userRepository.findByLoginId("admin")).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);
        given(accessLogRepository.save(any())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);

        assertThat(activeUser.getLoginFailCnt()).isEqualTo(1);
    }

    @Test
    @DisplayName("잠긴 계정으로 로그인 시 ACCOUNT_LOCKED 예외가 발생한다")
    void login_lockedAccount_throwsAccountLocked() {
        // given
        activeUser.lock();
        ReflectionTestUtils.setField(activeUser, "lastLoginAt", LocalDateTime.now());

        LoginRequest request = new LoginRequest("admin", "password123");
        given(userRepository.findByLoginId("admin")).willReturn(Optional.of(activeUser));
        given(accessLogRepository.save(any())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_LOCKED);
    }

    @Test
    @DisplayName("5회 실패 시 계정이 잠긴다")
    void login_fiveFailures_locksAccount() {
        // given
        ReflectionTestUtils.setField(activeUser, "loginFailCnt", 4);
        LoginRequest request = new LoginRequest("admin", "wrongPassword");
        given(userRepository.findByLoginId("admin")).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);
        given(accessLogRepository.save(any())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_LOCKED);

        assertThat(activeUser.isLocked()).isTrue();
    }

    @Test
    @DisplayName("30분 경과 후 잠긴 계정이 자동 해제된다")
    void login_autoUnlockAfter30Minutes() {
        // given
        activeUser.lock();
        ReflectionTestUtils.setField(activeUser, "lastLoginAt", LocalDateTime.now().minusMinutes(31));

        LoginRequest request = new LoginRequest("admin", "password123");
        given(userRepository.findByLoginId("admin")).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
        given(userRoleRepository.findByUserIdWithRole(1L)).willReturn(List.of(userRole));
        given(jwtTokenProvider.createAccessToken(eq(1L), eq("admin"), anyList()))
                .willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(1L)).willReturn("refresh-token");
        given(accessLogRepository.save(any())).willReturn(null);

        // when
        LoginResponse response = authService.login(request, "127.0.0.1");

        // then
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(activeUser.isLocked()).isFalse();
    }

    @Test
    @DisplayName("비밀번호 변경 시 정책 위반이면 예외가 발생한다")
    void changePassword_invalidPolicy_throwsException() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("currentPwd", "encodedPassword")).willReturn(true);
        ChangePasswordRequest request = new ChangePasswordRequest("currentPwd", "weak");

        // when & then
        assertThatThrownBy(() -> authService.changePassword(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("유효한 비밀번호로 변경 시 성공한다")
    void changePassword_validPolicy_success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("currentPwd", "encodedPassword")).willReturn(true);
        given(passwordEncoder.encode("NewPass1!")).willReturn("newEncodedPassword");
        ChangePasswordRequest request = new ChangePasswordRequest("currentPwd", "NewPass1!");

        // when
        authService.changePassword(1L, request);

        // then
        assertThat(activeUser.getPassword()).isEqualTo("newEncodedPassword");
        assertThat(activeUser.getPwdChangedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그인 시 UNAUTHORIZED 예외가 발생한다")
    void login_userNotFound_throwsUnauthorized() {
        // given
        LoginRequest request = new LoginRequest("unknown", "password123");
        given(userRepository.findByLoginId("unknown")).willReturn(Optional.empty());
        given(accessLogRepository.save(any())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);

        verify(accessLogRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("DELETED 상태의 계정으로 로그인 시 UNAUTHORIZED 예외가 발생한다")
    void login_deletedAccount_throwsUnauthorized() {
        // given
        ReflectionTestUtils.setField(activeUser, "status", UserStatus.DELETED);
        LoginRequest request = new LoginRequest("admin", "password123");
        given(userRepository.findByLoginId("admin")).willReturn(Optional.of(activeUser));
        given(accessLogRepository.save(any())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("리프레시 토큰이 유효하면 새로운 액세스 토큰을 발급한다")
    void refresh_validToken_returnsNewAccessToken() {
        // given
        String refreshToken = "valid-refresh-token";
        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserId(refreshToken)).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser));
        given(userRoleRepository.findByUserIdWithRole(1L)).willReturn(List.of(userRole));
        given(jwtTokenProvider.createAccessToken(eq(1L), eq("admin"), anyList()))
                .willReturn("new-access-token");

        // when
        LoginResponse response = authService.refresh(refreshToken);

        // then
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getRoles()).containsExactly("ADMIN");
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰이면 INVALID_TOKEN 예외가 발생한다")
    void refresh_invalidToken_throwsInvalidToken() {
        // given
        String refreshToken = "invalid-token";
        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("리프레시 토큰의 사용자가 존재하지 않으면 UNAUTHORIZED 예외가 발생한다")
    void refresh_userNotFound_throwsUnauthorized() {
        // given
        String refreshToken = "valid-refresh-token";
        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserId(refreshToken)).willReturn(999L);
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("로그아웃 시 LOGOUT 액세스 로그가 기록된다")
    void logout_success_recordsAccessLog() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser));
        given(accessLogRepository.save(any())).willReturn(null);

        // when
        authService.logout(1L, "127.0.0.1");

        // then
        verify(accessLogRepository, times(1)).save(argThat(log ->
                "LOGOUT".equals(log.getActionType()) && "Y".equals(log.getSuccessYn())));
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 로그아웃 시 ENTITY_NOT_FOUND 예외가 발생한다")
    void logout_userNotFound_throwsEntityNotFound() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.logout(999L, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("getMe 호출 시 사용자 정보와 부서/회사 정보를 반환한다")
    void getMe_withDepartment_returnsFullInfo() {
        // given
        Company company = Company.builder().companyNm("회사").build();
        Department dept = Department.builder().deptNm("부서").company(company).build();
        ReflectionTestUtils.setField(activeUser, "department", dept);
        ReflectionTestUtils.setField(activeUser, "email", "admin@test.com");
        ReflectionTestUtils.setField(activeUser, "pwdChangedAt", LocalDateTime.now().minusDays(10));

        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser));
        given(userRoleRepository.findByUserIdWithRole(1L)).willReturn(List.of(userRole));

        // when
        UserInfoResponse response = authService.getMe(1L);

        // then
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getLoginId()).isEqualTo("admin");
        assertThat(response.getEmail()).isEqualTo("admin@test.com");
        assertThat(response.getDeptName()).isEqualTo("부서");
        assertThat(response.getCompanyName()).isEqualTo("회사");
        assertThat(response.getRoles()).containsExactly("ADMIN");
        assertThat(response.isMustChangePassword()).isFalse();
    }

    @Test
    @DisplayName("getMe 호출 시 비밀번호 변경일이 null이면 mustChangePassword=true")
    void getMe_pwdChangedAtNull_mustChangePasswordTrue() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser));
        given(userRoleRepository.findByUserIdWithRole(1L)).willReturn(List.of(userRole));

        // when
        UserInfoResponse response = authService.getMe(1L);

        // then
        assertThat(response.isMustChangePassword()).isTrue();
    }

    @Test
    @DisplayName("getMe 호출 시 비밀번호 변경 후 90일 경과면 mustChangePassword=true")
    void getMe_pwdExpired90Days_mustChangePasswordTrue() {
        // given
        ReflectionTestUtils.setField(activeUser, "pwdChangedAt", LocalDateTime.now().minusDays(91));
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser));
        given(userRoleRepository.findByUserIdWithRole(1L)).willReturn(List.of(userRole));

        // when
        UserInfoResponse response = authService.getMe(1L);

        // then
        assertThat(response.isMustChangePassword()).isTrue();
    }

    @Test
    @DisplayName("getMe 호출 시 존재하지 않는 사용자면 ENTITY_NOT_FOUND 예외가 발생한다")
    void getMe_userNotFound_throwsEntityNotFound() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.getMe(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호 변경 시 현재 비밀번호가 일치하지 않으면 UNAUTHORIZED 예외가 발생한다")
    void changePassword_wrongCurrentPassword_throwsUnauthorized() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("wrongCurrent", "encodedPassword")).willReturn(false);
        ChangePasswordRequest request = new ChangePasswordRequest("wrongCurrent", "NewPass1!");

        // when & then
        assertThatThrownBy(() -> authService.changePassword(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("비밀번호 변경 시 사용자가 존재하지 않으면 ENTITY_NOT_FOUND 예외가 발생한다")
    void changePassword_userNotFound_throwsEntityNotFound() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());
        ChangePasswordRequest request = new ChangePasswordRequest("currentPwd", "NewPass1!");

        // when & then
        assertThatThrownBy(() -> authService.changePassword(999L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }
}
