package com.itsm.api.util;

import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import org.springframework.security.core.Authentication;

public final class AuthUtils {

    private AuthUtils() {}

    public static Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보가 없습니다.");
        }
        return (Long) authentication.getPrincipal();
    }
}
