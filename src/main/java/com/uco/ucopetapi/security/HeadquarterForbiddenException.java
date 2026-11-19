package com.uco.ucopetapi.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class HeadquarterForbiddenException extends ResponseStatusException {

    public HeadquarterForbiddenException(final String reason) {
        super(HttpStatus.FORBIDDEN, reason);
    }
}
