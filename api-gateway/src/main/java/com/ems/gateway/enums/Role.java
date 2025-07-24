package com.ems.gateway.enums;

public enum Role {
    ADMIN(1, "Admin"),
    MANAGER(2, "Manager"),
    EMPLOYEE(3, "Employee"),
    NA(4, "NA");

    private final int id;
    private final String name;

    Role(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static Role fromId(int id) {
        for (Role role : Role.values()) {
            if (role.getId() == id) {
                return role;
            }
        }
        return NA;
    }

    public static Role fromString(String roleStr) {
        try {
            int roleId = Integer.parseInt(roleStr);
            return fromId(roleId);
        } catch (NumberFormatException e) {
            return NA;
        }
    }
}
