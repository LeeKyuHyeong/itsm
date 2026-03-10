package com.itsm.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BusinessExceptionTest {

    @Test
    @DisplayName("ErrorCode로 BusinessException 생성 시 메시지와 코드가 올바르게 설정된다")
    void createWithErrorCode() {
        BusinessException ex = new BusinessException(ErrorCode.ENTITY_NOT_FOUND);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("ErrorCode와 커스텀 메시지로 BusinessException 생성 시 커스텀 메시지가 설정된다")
    void createWithCustomMessage() {
        BusinessException ex = new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다");

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("BusinessException은 RuntimeException이다")
    void isRuntimeException() {
        BusinessException ex = new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("ErrorCode의 HTTP 상태와 코드가 올바르게 반환된다")
    void errorCodeProperties() {
        assertThat(ErrorCode.ENTITY_NOT_FOUND.getStatus()).isEqualTo(404);
        assertThat(ErrorCode.ENTITY_NOT_FOUND.getCode()).isEqualTo("E404_001");

        assertThat(ErrorCode.INVALID_INPUT_VALUE.getStatus()).isEqualTo(400);
        assertThat(ErrorCode.UNAUTHORIZED.getStatus()).isEqualTo(401);
        assertThat(ErrorCode.ACCESS_DENIED.getStatus()).isEqualTo(403);
        assertThat(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).isEqualTo(500);
    }
}
