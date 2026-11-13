package com.uco.ucopetapi.controllers.appointment;

import com.uco.ucopetapi.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

// Convierte los errores del modulo de citas en un JSON con el campo "message".
// Sin este manejador, Spring Boot no incluye el texto de ResponseStatusException en la respuesta
// y el frontend no podria mostrar al usuario por que se rechazo la cita (fecha pasada, horario ocupado, etc.).
// Esta acotado al paquete de citas: no afecta a los demas modulos.
@RestControllerAdvice(basePackages = "com.uco.ucopetapi.controllers.appointment")
public class AppointmentExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex,
                                                            HttpServletRequest request) {
        int status = ex.getStatusCode().value();
        String message = ex.getReason() != null ? ex.getReason() : reasonPhrase(status);
        return build(status, message, request);
    }

    // Los servicios de doctor, tutor y mascota lanzan BusinessException cuando algo no existe o no es valido.
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request);
    }

    // Fecha u hora con formato incorrecto (se espera AAAA-MM-DD y HH:mm o HH:mm:ss) o JSON mal formado.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST.value(),
                "El cuerpo de la peticion no es un JSON valido. La fecha debe ser AAAA-MM-DD y la hora HH:mm",
                request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                  HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST.value(),
                "El parametro '" + ex.getName() + "' tiene un valor invalido", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException ex,
                                                                      HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST.value(),
                "Falta el parametro obligatorio '" + ex.getParameterName() + "'", request);
    }

    private ResponseEntity<Map<String, Object>> build(int status, String message, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status);
        body.put("error", reasonPhrase(status));
        body.put("message", message);
        body.put("path", request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    private String reasonPhrase(int status) {
        HttpStatus resolved = HttpStatus.resolve(status);
        return resolved != null ? resolved.getReasonPhrase() : String.valueOf(status);
    }
}
