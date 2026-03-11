package com.itsm.api.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @NotBlank(message = "사용자명은 필수입니다.")
    private String userNm;

    private String employeeNo;

    private Long deptId;

    private String email;

    private String tel;
}
