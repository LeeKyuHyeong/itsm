package com.itsm.api.dto.asset;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssetSwUpdateRequest {

    @NotBlank(message = "소프트웨어명은 필수입니다.")
    private String swNm;

    @NotBlank(message = "SW유형코드는 필수입니다.")
    private String swTypeCd;

    private String version;
    private String licenseKey;
    private Integer licenseCnt;
    private LocalDate installedAt;
    private LocalDate expiredAt;
    private Long managerId;
    private String description;
}
