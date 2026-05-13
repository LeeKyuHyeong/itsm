package com.itsm.api.dto.servicerequest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SrScheduleRequest {

    @NotNull(message = "처리예정일은 필수입니다.")
    private LocalDateTime scheduledAt;

    @Size(max = 500, message = "변경 사유는 500자 이내여야 합니다.")
    private String reason;
}
