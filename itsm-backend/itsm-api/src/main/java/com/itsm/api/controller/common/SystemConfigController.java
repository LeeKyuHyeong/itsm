package com.itsm.api.controller.common;

import com.itsm.api.dto.common.SystemConfigResponse;
import com.itsm.api.dto.common.SystemConfigUpdateRequest;
import com.itsm.api.service.common.SystemConfigService;
import com.itsm.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/system-configs")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    public ApiResponse<List<SystemConfigResponse>> getAllConfigs() {
        return ApiResponse.success(systemConfigService.getAllConfigs());
    }

    @GetMapping("/{configKey}")
    public ApiResponse<SystemConfigResponse> getConfig(@PathVariable String configKey) {
        return ApiResponse.success(systemConfigService.getConfig(configKey));
    }

    @PatchMapping("/{configKey}")
    public ApiResponse<SystemConfigResponse> updateConfig(
            @PathVariable String configKey,
            @Valid @RequestBody SystemConfigUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(systemConfigService.updateConfig(configKey, req, currentUserId));
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
