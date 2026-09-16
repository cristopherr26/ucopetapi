package com.uco.ucopetapi.service.purchases.exception;

import java.util.UUID;

public class HeadquarterNotFoundException extends RuntimeException {
    public HeadquarterNotFoundException(UUID id) {
        super("No existe una sede con id " + id);
    }
}
