package com.itsm.api.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigResponse {

    private Long configId;
    private String configKey;
    private String configVal;
    private String description;
    private LocalDateTime updatedAt;
}
