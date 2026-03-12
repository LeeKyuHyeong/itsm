package com.itsm.api.dto.change;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "변경 내용은 필수입니다.")
    private String content;

    @NotBlank(message = "변경유형코드는 필수입니다.")
    private String changeTypeCd;

    @NotBlank(message = "우선순위코드는 필수입니다.")
    private String priorityCd;

    private LocalDateTime scheduledAt;

    private String rollbackPlan;
}
