package com.uco.ucopetapi.exception.healthPlan;

public class DuplicateCoverageException extends RuntimeException {
    public DuplicateCoverageException() {
        super("This procedure already has a coverage in this health plan");
    }
}
