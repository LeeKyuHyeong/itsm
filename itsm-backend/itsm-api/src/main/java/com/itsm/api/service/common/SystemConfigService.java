package com.itsm.api.service.common;

import com.itsm.api.dto.common.SystemConfigResponse;
import com.itsm.api.dto.common.SystemConfigUpdateRequest;
import com.itsm.core.domain.common.SystemConfig;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

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
