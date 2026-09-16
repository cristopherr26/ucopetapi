package com.uco.ucopetapi.service.purchases.exception;

public class HeadquarterInactiveException extends RuntimeException {
    public HeadquarterInactiveException(String name) {
        super("La sede '" + name + "' no está activa, no se puede usar en una compra");
    }
}
