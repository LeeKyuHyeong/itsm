package com.itsm.core.constant;

public final class RoleCode {

    private RoleCode() {}

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ITSM_ADMIN = "ITSM_ADMIN";

    public static final String HAS_ADMIN_ROLE = "hasRole('SUPER_ADMIN') or hasRole('ITSM_ADMIN')";
}
