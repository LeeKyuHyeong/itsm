package com.itsm.api.service.common;

import com.itsm.api.dto.common.SystemConfigResponse;
import com.itsm.api.dto.common.SystemConfigUpdateRequest;
import com.itsm.core.domain.common.SystemConfig;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import com.itsm.core.constant.RoleCode;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    /** 2026-09-16 P1: ITSM.md 설정관리 표 — 시스템 설정은 관리자만(감사자 ❌). 이전엔 아무 인증 사용자나 잠금 횟수를 바꿀 수 있었다 */
    @PreAuthorize(RoleCode.HAS_ADMIN_ROLE)
    @Transactional(readOnly = true)
    public List<SystemConfigResponse> getAllConfigs() {
        return systemConfigRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SystemConfigResponse getConfig(String configKey) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "시스템 설정을 찾을 수 없습니다."));
        return toResponse(config);
    }

    /** 값 변경 즉시 SystemConfigReader 캐시를 비워 재시작 없이 반영되게 한다 (2026-09-16 P3) */
    @PreAuthorize(RoleCode.HAS_ADMIN_ROLE)
    @CacheEvict(value = "systemConfig", key = "#configKey")
    public SystemConfigResponse updateConfig(String configKey, SystemConfigUpdateRequest req, Long currentUserId) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "시스템 설정을 찾을 수 없습니다."));

        config.updateValue(req.getConfigVal(), currentUserId);
        return toResponse(config);
    }

    private SystemConfigResponse toResponse(SystemConfig config) {
        return SystemConfigResponse.builder()
                .configId(config.getConfigId())
                .configKey(config.getConfigKey())
                .configVal(config.getConfigVal())
                .description(config.getDescription())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
