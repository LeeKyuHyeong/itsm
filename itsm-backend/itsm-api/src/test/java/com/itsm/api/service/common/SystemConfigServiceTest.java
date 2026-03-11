package com.itsm.api.service.common;

import com.itsm.api.dto.common.SystemConfigResponse;
import com.itsm.api.dto.common.SystemConfigUpdateRequest;
import com.itsm.core.domain.common.SystemConfig;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.SystemConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SystemConfigServiceTest {

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @InjectMocks
    private SystemConfigService systemConfigService;

    private SystemConfig config;

    @BeforeEach
    void setUp() {
        config = SystemConfig.builder()
                .configKey("site.name")
                .configVal("ITSM System")
                .description("사이트 이름")
                .updatedBy(1L)
                .build();
        ReflectionTestUtils.setField(config, "configId", 1L);
    }

    @Test
    @DisplayName("전체 시스템 설정 목록을 조회한다")
    void getAllConfigs_returnsList() {
        // given
        given(systemConfigRepository.findAll()).willReturn(List.of(config));

        // when
        List<SystemConfigResponse> result = systemConfigService.getAllConfigs();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getConfigKey()).isEqualTo("site.name");
        assertThat(result.get(0).getConfigVal()).isEqualTo("ITSM System");
    }

    @Test
    @DisplayName("설정 키로 시스템 설정을 조회한다")
    void getConfig_returnsConfig() {
        // given
        given(systemConfigRepository.findByConfigKey("site.name")).willReturn(Optional.of(config));

        // when
        SystemConfigResponse result = systemConfigService.getConfig("site.name");

        // then
        assertThat(result.getConfigKey()).isEqualTo("site.name");
        assertThat(result.getConfigVal()).isEqualTo("ITSM System");
        assertThat(result.getDescription()).isEqualTo("사이트 이름");
    }

    @Test
    @DisplayName("존재하지 않는 설정 키로 조회 시 예외가 발생한다")
    void getConfig_unknownKey_throwsException() {
        // given
        given(systemConfigRepository.findByConfigKey("unknown.key")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> systemConfigService.getConfig("unknown.key"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("시스템 설정 값을 수정한다")
    void updateConfig_success() {
        // given
        SystemConfigUpdateRequest req = new SystemConfigUpdateRequest("New ITSM System");
        given(systemConfigRepository.findByConfigKey("site.name")).willReturn(Optional.of(config));

        // when
        SystemConfigResponse result = systemConfigService.updateConfig("site.name", req, 1L);

        // then
        assertThat(result.getConfigVal()).isEqualTo("New ITSM System");
    }

    @Test
    @DisplayName("존재하지 않는 설정 키로 수정 시 예외가 발생한다")
    void updateConfig_unknownKey_throwsException() {
        // given
        SystemConfigUpdateRequest req = new SystemConfigUpdateRequest("value");
        given(systemConfigRepository.findByConfigKey("unknown.key")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> systemConfigService.updateConfig("unknown.key", req, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }
}
