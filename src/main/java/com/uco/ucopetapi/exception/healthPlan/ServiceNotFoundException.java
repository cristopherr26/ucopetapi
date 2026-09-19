package com.uco.ucopetapi.exception.healthPlan;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException() {
        super("service not found or inactive");
    }
}
