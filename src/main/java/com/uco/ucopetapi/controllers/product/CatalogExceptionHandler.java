package com.uco.ucopetapi.controllers.product;

import com.uco.ucopetapi.dto.product.CatalogErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.uco.ucopetapi.controllers.product")
public class CatalogExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<CatalogErrorDTO> handleStatus(ResponseStatusException ex, HttpServletRequest request) {
        return build(ex.getStatusCode().value(), ex.getReason(), request);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<CatalogErrorDTO> handleNotFound(NoSuchElementException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CatalogErrorDTO> handleValidation(MethodArgumentNotValidException ex,
                                                            HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST.value(), message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CatalogErrorDTO> handleConstraint(ConstraintViolationException ex,
            HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> lastNode(violation.getPropertyPath().toString()) + ": " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST.value(), message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CatalogErrorDTO> handleUnreadable(HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST.value(),
                "El cuerpo de la petición no es un JSON válido o tiene un valor no permitido", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CatalogErrorDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST.value(),
                "El parámetro '" + ex.getName() + "' tiene un valor inválido", request);
    }

    private static String lastNode(String path) {
        return path.substring(path.lastIndexOf('.') + 1);
    }

    private ResponseEntity<CatalogErrorDTO> build(int status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(CatalogErrorDTO.of(status, message, request.getRequestURI()));
    }
}
