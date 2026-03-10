package com.itsm.core.dto;

import com.itsm.core.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    @DisplayName("성공 응답 생성 - 데이터 포함")
    void successWithData() {
        ApiResponse<String> response = ApiResponse.success("hello");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("hello");
        assertThat(response.getError()).isNull();
    }

    @Test
    @DisplayName("성공 응답 생성 - 데이터 없음")
    void successWithoutData() {
        ApiResponse<Void> response = ApiResponse.success();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isNull();
    }

    @Test
    @DisplayName("에러 응답 생성 - ErrorCode로")
    void errorWithErrorCode() {
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isNotNull();
        assertThat(response.getError().getCode()).isEqualTo("E404_001");
        assertThat(response.getError().getMessage()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("에러 응답 생성 - 커스텀 메시지")
    void errorWithCustomMessage() {
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getMessage()).isEqualTo("사용자를 찾을 수 없습니다");
    }
}
