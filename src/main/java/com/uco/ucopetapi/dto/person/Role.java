package com.uco.ucopetapi.dto.person;

import java.util.Optional;

public enum Role {

    ADMIN,
    DOCTOR,
    TUTOR;

    private static final String PREFIJO_SPRING = "ROLE_";

    public String authority() {
        return PREFIJO_SPRING + name();
    }

    public static Optional<Role> de(String nombre) {
        try {
            return Optional.of(valueOf(nombre));
        } catch (IllegalArgumentException | NullPointerException _) {
            return Optional.empty();
        }
    }
}
