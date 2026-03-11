package com.itsm.core.domain.common;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_system_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_val", nullable = false, columnDefinition = "TEXT")
    private String configVal;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Builder
    public SystemConfig(String configKey, String configVal, String description, Long updatedBy) {
        this.configKey = configKey;
        this.configVal = configVal;
        this.description = description;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateValue(String configVal, Long updatedBy) {
        this.configVal = configVal;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }
}
