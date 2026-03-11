package com.itsm.api.dto.user;

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
public class UserDetailResponse {

    private Long userId;
    private String loginId;
    private String userNm;
    private String employeeNo;
    private Long deptId;
    private String deptName;
    private Long companyId;
    private String companyName;
    private String email;
    private String tel;
    private String status;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private LocalDateTime lastLoginAt;
    private LocalDateTime pwdChangedAt;
    private List<RoleInfo> roles;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {
        private Long roleId;
        private String roleNm;
        private String roleCd;
    }
}
