package com.uco.ucopetapi.controllers.notification;

import com.uco.ucopetapi.dto.transfers.ApiErrorDTO;
import com.uco.ucopetapi.exception.notification.NotificationAccessDeniedException;
import com.uco.ucopetapi.exception.notification.NotificationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.uco.ucopetapi.controllers.notification")
public class NotificationExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleNotFound(NotificationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorDTO.of(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(NotificationAccessDeniedException.class)
    public ResponseEntity<ApiErrorDTO> handleForbidden(NotificationAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorDTO.of(HttpStatus.FORBIDDEN.value(), ex.getMessage()));
    }
}