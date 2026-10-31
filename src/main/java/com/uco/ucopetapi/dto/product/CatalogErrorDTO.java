package com.uco.ucopetapi.dto.product;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record CatalogErrorDTO(Instant timestamp, int status, String error, String message, String path) {

    public static CatalogErrorDTO of(int status, String message, String path) {
        HttpStatus resolved = HttpStatus.resolve(status);
        String error = resolved != null ? resolved.getReasonPhrase() : String.valueOf(status);
        return new CatalogErrorDTO(Instant.now(), status, error, message, path);
    }
}
