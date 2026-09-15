package com.itsm.api.service.common;

import com.itsm.core.domain.common.SystemConfig;
import com.itsm.core.repository.common.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * tb_system_config 를 읽는 쪽 (관리자 화면의 쓰기는 SystemConfigService).
 * <p>
 * 2026-09-16 전수조사 P3: 관리자가 password.expire.days / login.fail.lock.count / password.min.length 를 바꿔도
 * 읽는 코드가 없어 상수(90·5·8)가 그대로 쓰였다. 이제 AuthService·UserService·PasswordExpiryInterceptor 가 여기서 읽는다.
 * 잘못된 값(비숫자)은 기본값으로 떨어져 로그인 자체가 막히지 않게 한다. "systemConfig" 캐시는 updateConfig 에서 비운다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemConfigReader {

    public static final String KEY_LOGIN_FAIL_LOCK_COUNT = "login.fail.lock.count";
    public static final String KEY_PASSWORD_EXPIRE_DAYS = "password.expire.days";
    public static final String KEY_PASSWORD_MIN_LENGTH = "password.min.length";

    private final SystemConfigRepository systemConfigRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "systemConfig", key = "#key")
    public String getString(String key, String defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigVal)
                .filter(v -> v != null && !v.isBlank())
                .orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public int getInt(String key, int defaultValue) {
        String raw = getString(key, null);
        if (raw == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            log.warn("시스템 설정 {} 값 '{}' 이(가) 정수가 아니라 기본값 {} 을 사용합니다.", key, raw, defaultValue);
            return defaultValue;
        }
    }
}
