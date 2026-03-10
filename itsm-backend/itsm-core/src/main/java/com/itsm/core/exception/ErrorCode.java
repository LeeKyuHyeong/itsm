package com.itsm.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    INVALID_INPUT_VALUE(400, "E400_001", "잘못된 입력값입니다."),
    INVALID_TYPE_VALUE(400, "E400_002", "잘못된 타입입니다."),
    DUPLICATE_VALUE(400, "E400_003", "중복된 값입니다."),
    INVALID_STATE_TRANSITION(400, "E400_004", "유효하지 않은 상태 전이입니다."),

    // 401 Unauthorized
    UNAUTHORIZED(401, "E401_001", "인증이 필요합니다."),
    INVALID_TOKEN(401, "E401_002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "E401_003", "만료된 토큰입니다."),

    // 403 Forbidden
    ACCESS_DENIED(403, "E403_001", "접근 권한이 없습니다."),

    // 404 Not Found
    ENTITY_NOT_FOUND(404, "E404_001", "대상을 찾을 수 없습니다."),

    // 409 Conflict
    OPTIMISTIC_LOCK_CONFLICT(409, "E409_001", "다른 사용자에 의해 데이터가 변경되었습니다. 새로고침 후 다시 시도해주세요."),

    // 423 Locked
    ACCOUNT_LOCKED(423, "E423_001", "계정이 잠겼습니다. 30분 후 다시 시도해주세요."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(500, "E500_001", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
