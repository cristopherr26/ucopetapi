package com.uco.ucopetapi.exception.healthplan;

public class HealthPlanNotFoundException extends RuntimeException {
    public HealthPlanNotFoundException() {
        super("Health plan not found");
    }
}