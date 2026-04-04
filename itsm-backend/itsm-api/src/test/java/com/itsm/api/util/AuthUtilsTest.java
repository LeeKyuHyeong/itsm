package com.itsm.api.util;

import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthUtilsTest {

    @Test
    @DisplayName("정상 Authentication에서 userId를 추출한다")
    void getCurrentUserId_success() {
        Authentication auth = new UsernamePasswordAuthenticationToken(1L, null);

        Long userId = AuthUtils.getCurrentUserId(auth);

        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("Authentication이 null이면 UNAUTHORIZED 예외를 던진다")
    void getCurrentUserId_nullAuth_throwsException() {
        assertThatThrownBy(() -> AuthUtils.getCurrentUserId(null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Principal이 null이면 UNAUTHORIZED 예외를 던진다")
    void getCurrentUserId_nullPrincipal_throwsException() {
        Authentication auth = new UsernamePasswordAuthenticationToken(null, null);

        assertThatThrownBy(() -> AuthUtils.getCurrentUserId(auth))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }
}
