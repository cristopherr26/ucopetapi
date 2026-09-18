package com.uco.ucopetapi.service.purchases.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(UUID id) {
        super("No existe un producto con id " + id);
    }
}
