package com.uco.ucopetapi.exception.healthPlan;

public class CoverageNotFoundException extends RuntimeException {
    public CoverageNotFoundException() {
        super("Coverage not found");
    }
}
