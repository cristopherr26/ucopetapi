package com.uco.ucopetapi.dto.transfers;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record ApiErrorDTO(int status, String message, LocalDateTime timestamp) {

    public static ApiErrorDTO of(int status, String message) {
        return new ApiErrorDTO(status, message, LocalDateTime.now(ZoneId.systemDefault()));
    }
}