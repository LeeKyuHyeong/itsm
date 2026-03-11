package com.itsm.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    private Long userId;
    private String loginId;
    private String userNm;
    private String email;
    private List<String> roles;
    private String deptName;
    private String companyName;
    private LocalDateTime pwdChangedAt;
    private boolean mustChangePassword;
}
