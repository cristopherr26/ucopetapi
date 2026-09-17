package com.uco.ucopetapi.exception.healthPlan;

public class ProcedureServiceException extends RuntimeException {

        public ProcedureServiceException(String message) {
            super(message);
        }

        public ProcedureServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

