package com.itsm.api.dto.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IncidentCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "장애 내용은 필수입니다.")
    private String content;

    @NotBlank(message = "장애유형코드는 필수입니다.")
    private String incidentTypeCd;

    @NotBlank(message = "우선순위코드는 필수입니다.")
    private String priorityCd;

    @NotNull(message = "장애 발생일시는 필수입니다.")
    private LocalDateTime occurredAt;

    @NotNull(message = "고객사ID는 필수입니다.")
    private Long companyId;

    private Long mainManagerId;

    private List<IncidentAssetRequest> assets;
}
