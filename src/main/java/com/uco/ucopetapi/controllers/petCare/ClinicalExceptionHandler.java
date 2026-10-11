package com.uco.ucopetapi.controllers.petCare;

import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.exception.clinical.ClinicalException;
import com.uco.ucopetapi.service.order.exception.InvalidOrderRequestException;
import com.uco.ucopetapi.service.order.exception.InvalidOrderStateException;
import com.uco.ucopetapi.service.order.exception.OrderNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = {
        "com.uco.ucopetapi.controllers.petCare",
        "com.uco.ucopetapi.controllers.episode"
})
public class ClinicalExceptionHandler {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Bogota");

    @ExceptionHandler(ClinicalException.class)
    public ResponseEntity<Map<String, Object>> handleClinical(final ClinicalException ex) {
        return body(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(final MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        if (message.isBlank()) {
            message = "La solicitud no es válida";
        }
        return body(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler({NoSuchElementException.class, OrderNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(final RuntimeException ex) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOrderState(final InvalidOrderStateException ex) {
        return body(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({BusinessException.class, InvalidOrderRequestException.class})
    public ResponseEntity<Map<String, Object>> handleBusiness(final BusinessException ex) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(final DataIntegrityViolationException ex) {
        return body(HttpStatus.CONFLICT, "La operación entra en conflicto con datos ya registrados");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(final ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String message = ex.getReason() != null ? ex.getReason() : "Error en la solicitud";
        return body(status, message);
    }

    private ResponseEntity<Map<String, Object>> body(final HttpStatusCode status, final String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now(ZONE_ID));
        response.put("status", status.value());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}

