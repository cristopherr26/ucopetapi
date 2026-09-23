package com.uco.ucopetapi.exception.healthPlan;

public class CoverageLimitExceededException extends RuntimeException {
    public CoverageLimitExceededException() {
        super("The health plan cannot have more coverages than available procedures");
    }
}
