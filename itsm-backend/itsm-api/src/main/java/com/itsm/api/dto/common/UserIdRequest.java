package com.itsm.api.dto.common;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserIdRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;
}
