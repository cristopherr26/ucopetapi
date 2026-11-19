package com.uco.ucopetapi.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.uco.ucopetapi.dto.person.PersonErrorDTO;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class HeadquarterScopeExceptionHandler {

    @ExceptionHandler(HeadquarterForbiddenException.class)
    public ResponseEntity<PersonErrorDTO> handleForbidden(final HeadquarterForbiddenException ex,
                                                          final HttpServletRequest request) {
        int status = HttpStatus.FORBIDDEN.value();
        return ResponseEntity.status(status)
                .body(PersonErrorDTO.of(status, ex.getReason(), request.getRequestURI()));
    }
}
