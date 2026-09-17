package com.uco.ucopetapi.exception.healthplan;

public class CoverageNotFoundException extends RuntimeException {
    public CoverageNotFoundException() {
        super("Coverage not found");
    }
}
