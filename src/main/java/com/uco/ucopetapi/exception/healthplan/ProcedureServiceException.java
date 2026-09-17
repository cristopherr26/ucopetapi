package com.uco.ucopetapi.exception.healthplan;

public class ProcedureServiceException extends RuntimeException {

        public ProcedureServiceException(String message) {
            super(message);
        }

        public ProcedureServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

