package com.uco.ucopetapi.exception.healthPlan;

public class DuplicateCoverageException extends RuntimeException {
    public DuplicateCoverageException() {
        super("This service already has a coverage in this health plan");
    }
}
