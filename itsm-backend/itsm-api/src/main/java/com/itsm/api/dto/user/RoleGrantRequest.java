package com.itsm.api.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 역할 부여 요청. roleId(숫자) 또는 roleCd('ROLE_PM') 중 하나면 된다.
 * 2026-09-16 전수조사 P2: 프론트(AccountManageView/RoleManageModal)는 역할 코드만 알고 있는데
 * 백엔드는 roleId 만 @NotNull 로 받아 역할 부여가 항상 400 이었다.
 */
@Getter
@NoArgsConstructor
public class RoleGrantRequest {

    private Long roleId;
    private String roleCd;

    public RoleGrantRequest(Long roleId) {
        this.roleId = roleId;
    }

    public RoleGrantRequest(Long roleId, String roleCd) {
        this.roleId = roleId;
        this.roleCd = roleCd;
    }
}
