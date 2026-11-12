package com.uco.ucopetapi.controllers.receipts.exception;

import com.uco.ucopetapi.service.receipts.ReceiptNotFoundException;
import com.uco.ucopetapi.service.receipts.ReceiptStateConflictException;
import com.uco.ucopetapi.service.receipts.ReceiptValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.uco.ucopetapi.controllers.receipts")
public class ReceiptExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ReceiptExceptionHandler.class);
    private static final ZoneId ZONE = ZoneId.of("America/Bogota");

    @ExceptionHandler(ReceiptNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(final ReceiptNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ReceiptValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(final ReceiptValidationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ReceiptStateConflictException.class)
    public ResponseEntity<Map<String, Object>> handleStateConflict(final ReceiptStateConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleBeanValidation(final MethodArgumentNotValidException ex) {
        final String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMessageNotReadable(final HttpMessageNotReadableException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud contiene datos inválidos o mal formados.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(final MethodArgumentTypeMismatchException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "El formato de '" + ex.getName() + "' no es válido.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(final MissingServletRequestParameterException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Falta el parámetro '" + ex.getParameterName() + "'.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(final AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "No tienes permiso para gestionar recibos de caja.");
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<Map<String, Object>> handleBrokerUnavailable(final AmqpException ex) {
        log.error("No se pudo publicar el mensaje del recibo en RabbitMQ", ex);
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE,
                "El procesador de pagos no está disponible en este momento. Intenta más tarde.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(final Exception ex) {
        log.error("Error inesperado procesando una solicitud de recibos", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado procesando la solicitud.");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(final HttpStatus status, final String message) {
        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now(ZONE));
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}