package com.itsm.api.interceptor;

import com.itsm.core.domain.user.User;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PasswordExpiryInterceptorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PasswordExpiryInterceptor interceptor;

    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private User makeUser(LocalDateTime pwdChangedAt) {
        User user = User.builder()
                .loginId("u")
                .password("p")
                .userNm("n")
                .status("ACTIVE")
                .build();
        ReflectionTestUtils.setField(user, "userId", 1L);
        ReflectionTestUtils.setField(user, "pwdChangedAt", pwdChangedAt);
        return user;
    }

    @Test
    @DisplayName("/api/v1/auth/** 경로는 검증 없이 통과한다")
    void skip_authPath() {
        given(request.getRequestURI()).willReturn("/api/v1/auth/login");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Swagger 관련 경로는 검증 없이 통과한다")
    void skip_swaggerPath() {
        given(request.getRequestURI()).willReturn("/swagger-ui/index.html");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("인증되지 않은 요청은 통과한다 (AuthInterceptor가 별도로 차단)")
    void unauthenticated_passesThrough() {
        given(request.getRequestURI()).willReturn("/api/v1/users");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("principal이 Long이 아니면 통과한다")
    void nonLongPrincipal_passesThrough() {
        given(request.getRequestURI()).willReturn("/api/v1/users");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("string-principal", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("사용자가 DB에 없으면 통과한다")
    void userNotFound_passesThrough() {
        given(request.getRequestURI()).willReturn("/api/v1/users");
        setAuthentication(999L);
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("비밀번호 변경일이 90일 이내이면 통과한다")
    void recentPasswordChange_passes() {
        given(request.getRequestURI()).willReturn("/api/v1/users");
        setAuthentication(1L);
        User user = makeUser(LocalDateTime.now().minusDays(30));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("비밀번호 변경일이 90일 초과이면 PASSWORD_EXPIRED 예외가 발생한다")
    void expiredPassword_throwsException() {
        given(request.getRequestURI()).willReturn("/api/v1/users");
        setAuthentication(1L);
        User user = makeUser(LocalDateTime.now().minusDays(91));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PASSWORD_EXPIRED);
    }

    @Test
    @DisplayName("pwdChangedAt이 null이면 PASSWORD_EXPIRED 예외가 발생한다")
    void nullPwdChangedAt_throwsException() {
        given(request.getRequestURI()).willReturn("/api/v1/users");
        setAuthentication(1L);
        User user = makeUser(null);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PASSWORD_EXPIRED);
    }

    @Test
    @DisplayName("비밀번호 변경일이 정확히 90일 전이면 통과한다 (경계값)")
    void exactly90Days_passes() {
        given(request.getRequestURI()).willReturn("/api/v1/users");
        setAuthentication(1L);
        // 정확히 90일 전 - 90 + 90 = 180일 후가 만료 시점이 되도록
        User user = makeUser(LocalDateTime.now().minusDays(89));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }
}
