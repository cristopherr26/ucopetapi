package com.uco.ucopetapi.exception.healthplan;

public class CoverageLimitExceededException extends RuntimeException {
    public CoverageLimitExceededException() {
        super("The health plan cannot have more coverages than available procedures");
    }
}
