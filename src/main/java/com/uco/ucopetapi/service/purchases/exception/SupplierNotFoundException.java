package com.uco.ucopetapi.service.purchases.exception;

import java.util.UUID;

public class SupplierNotFoundException extends RuntimeException {
    public SupplierNotFoundException(UUID id) {
        super("No existe un proveedor con id " + id);
    }
}
