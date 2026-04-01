package com.itsm.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    @Size(max = 128, message = "비밀번호는 128자를 초과할 수 없습니다.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Size(max = 128, message = "비밀번호는 128자를 초과할 수 없습니다.")
    private String newPassword;
}
