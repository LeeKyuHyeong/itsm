package com.itsm.core.constant;

public final class RoleCode {

    private RoleCode() {}

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ITSM_ADMIN = "ITSM_ADMIN";

    public static final String HAS_ADMIN_ROLE = "hasRole('SUPER_ADMIN') or hasRole('ITSM_ADMIN')";

    public static final String AUTHORITY_PREFIX = "ROLE_";

    /**
     * tb_role.role_cd 를 Spring Security authority 문자열로 바꾼다.
     * role_cd 는 Phase 0 시드부터 'ROLE_SUPER_ADMIN' 처럼 접두사를 포함하므로 접두사가 없을 때만 붙인다.
     * 접두사를 무조건 붙이면 'ROLE_ROLE_SUPER_ADMIN' 이 되어 hasRole('SUPER_ADMIN') 이 영원히 실패한다(2026-09-17 운영에서 발견).
     */
    public static String toAuthority(String roleCd) {
        if (roleCd == null) {
            return null;
        }
        return roleCd.startsWith(AUTHORITY_PREFIX) ? roleCd : AUTHORITY_PREFIX + roleCd;
    }
}
