package com.breeding.common;

import java.util.Set;

public final class UserRoleConstants {

    public static final String ADMIN = "admin";
    public static final String OWNER = "owner";
    public static final String VET = "vet";
    public static final String FEEDER = "feeder";

    public static final Set<String> MANAGE_ROLE_CODES = Set.of(ADMIN, OWNER, VET, FEEDER);
    public static final Set<String> ASSIGNABLE_ROLE_CODES = Set.of(OWNER, VET, FEEDER);
    public static final Set<String> REGISTER_ROLE_CODES = Set.of(VET, FEEDER);

    private UserRoleConstants() {
    }

    public static boolean isManageRole(String roleCode) {
        return roleCode != null && MANAGE_ROLE_CODES.contains(roleCode);
    }

    public static boolean isAssignableRole(String roleCode) {
        return roleCode != null && ASSIGNABLE_ROLE_CODES.contains(roleCode);
    }

    public static boolean isRegisterRole(String roleCode) {
        return roleCode != null && REGISTER_ROLE_CODES.contains(roleCode);
    }
}
