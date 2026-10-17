package com.uco.ucopetapi.dto.person;

import java.time.Instant;

import org.springframework.http.HttpStatus;

public record PersonErrorDTO(Instant timestamp, int status, String error, String message, String path) {

    public static PersonErrorDTO of(int status, String message, String path) {
        HttpStatus resolved = HttpStatus.resolve(status);
        String error = resolved != null ? resolved.getReasonPhrase() : String.valueOf(status);
        return new PersonErrorDTO(Instant.now(), status, error, message, path);
    }
}