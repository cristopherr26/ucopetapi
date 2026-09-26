package com.uco.ucopetapi.service.product;

public class DuplicateCatalogNameException extends RuntimeException {

    public DuplicateCatalogNameException(String item) {
        super("Ya existe un " + item + " con este nombre");
    }
}
