package com.itsm.api.service.common;

import com.itsm.core.domain.common.SystemConfig;
import com.itsm.core.repository.common.SystemConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * 2026-09-16 전수조사 P3 — tb_system_config 의 password.expire.days / login.fail.lock.count / password.min.length 는
 * 관리자 화면에서 바꿀 수 있었지만 읽는 코드가 0 이었다(AuthService·PasswordExpiryInterceptor 는 상수 5·90 하드코딩).
 */
@ExtendWith(MockitoExtension.class)
class SystemConfigReaderTest {

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @InjectMocks
    private SystemConfigReader reader;

    private SystemConfig config(String key, String val) {
        return SystemConfig.builder().configKey(key).configVal(val).description("d").build();
    }

    @Test
    @DisplayName("getInt - DB 값이 있으면 정수로 파싱해 돌려준다")
    void getInt_returnsParsedValue() {
        given(systemConfigRepository.findByConfigKey("login.fail.lock.count"))
                .willReturn(Optional.of(config("login.fail.lock.count", "3")));

        assertThat(reader.getInt("login.fail.lock.count", 5)).isEqualTo(3);
    }

    @Test
    @DisplayName("getInt - 키가 없으면 기본값")
    void getInt_missingKey_returnsDefault() {
        given(systemConfigRepository.findByConfigKey("password.expire.days")).willReturn(Optional.empty());

        assertThat(reader.getInt("password.expire.days", 90)).isEqualTo(90);
    }

    @Test
    @DisplayName("getInt - 숫자가 아니거나 공백이면 기본값 (관리자 오입력이 로그인 자체를 막지 않게)")
    void getInt_nonNumeric_returnsDefault() {
        given(systemConfigRepository.findByConfigKey("password.min.length"))
                .willReturn(Optional.of(config("password.min.length", " abc ")));

        assertThat(reader.getInt("password.min.length", 8)).isEqualTo(8);
    }

    @Test
    @DisplayName("getInt - 앞뒤 공백은 허용한다")
    void getInt_trimsWhitespace() {
        given(systemConfigRepository.findByConfigKey("password.min.length"))
                .willReturn(Optional.of(config("password.min.length", " 12 ")));

        assertThat(reader.getInt("password.min.length", 8)).isEqualTo(12);
    }

    @Test
    @DisplayName("getString - 값이 없으면 기본값")
    void getString_missing_returnsDefault() {
        given(systemConfigRepository.findByConfigKey("system.name")).willReturn(Optional.empty());

        assertThat(reader.getString("system.name", "ITSM")).isEqualTo("ITSM");
    }
}
