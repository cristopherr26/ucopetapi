package com.uco.ucopetapi.exception.healthPlan;

public class ServiceServiceException extends RuntimeException {

        public ServiceServiceException(String message) {
            super(message);
        }

        public ServiceServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

