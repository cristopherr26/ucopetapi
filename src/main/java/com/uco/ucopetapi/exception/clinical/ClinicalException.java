package com.uco.ucopetapi.exception.clinical;

import org.springframework.http.HttpStatus;

public class ClinicalException extends RuntimeException {

    private final HttpStatus status;

    private ClinicalException(final HttpStatus status, final String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ClinicalException badRequest(final String message) {
        return new ClinicalException(HttpStatus.BAD_REQUEST, message);
    }

    public static ClinicalException forbidden(final String message) {
        return new ClinicalException(HttpStatus.FORBIDDEN, message);
    }

    public static ClinicalException notFound(final String message) {
        return new ClinicalException(HttpStatus.NOT_FOUND, message);
    }

    public static ClinicalException conflict(final String message) {
        return new ClinicalException(HttpStatus.CONFLICT, message);
    }
}

