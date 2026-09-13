package com.uco.ucopetapi.dto.person;

import java.util.Optional;

public enum Role {

    ADMIN,
    DOCTOR;

    private static final String SPRING_PREFIX = "ROLE_";

    public String authority() {
        return SPRING_PREFIX + name();
    }

    public static Optional<Role> from(String name) {
        try {
            return Optional.of(valueOf(name));
        } catch (IllegalArgumentException | NullPointerException _) {
            return Optional.empty();
        }
    }
}
