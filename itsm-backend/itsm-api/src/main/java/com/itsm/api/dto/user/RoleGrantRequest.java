package com.itsm.api.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoleGrantRequest {

    @NotNull(message = "역할 ID는 필수입니다.")
    private Long roleId;
}
