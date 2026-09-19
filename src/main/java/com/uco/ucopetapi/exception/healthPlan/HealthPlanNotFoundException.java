package com.uco.ucopetapi.exception.healthPlan;

public class HealthPlanNotFoundException extends RuntimeException {
    public HealthPlanNotFoundException() {
        super("Health plan not found");
    }
}