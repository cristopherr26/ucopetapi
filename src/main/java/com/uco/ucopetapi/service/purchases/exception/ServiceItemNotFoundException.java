package com.uco.ucopetapi.service.purchases.exception;

import java.util.UUID;

public class ServiceItemNotFoundException extends RuntimeException {
    public ServiceItemNotFoundException(UUID id) {
        super("No existe un servicio con id " + id);
    }
}
