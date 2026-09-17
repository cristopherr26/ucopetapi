package com.uco.ucopetapi.exception.healthPlan;

public class ProcedureNotFoundException extends RuntimeException {
    public ProcedureNotFoundException() {
        super("Procedure not found or inactive");
    }
}
