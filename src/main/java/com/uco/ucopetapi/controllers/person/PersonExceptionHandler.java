package com.uco.ucopetapi.controllers.person;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import com.uco.ucopetapi.dto.person.PersonErrorDTO;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackages = "com.uco.ucopetapi.controllers.person")
public class PersonExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<PersonErrorDTO> handleStatus(ResponseStatusException ex,
                                                       HttpServletRequest request) {
        return build(ex.getStatusCode().value(), ex.getReason(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PersonErrorDTO> handleValidation(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST.value(), message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<PersonErrorDTO> handleUnreadable(HttpMessageNotReadableException ex,
                                                           HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST.value(),
                "El cuerpo de la peticion no es un JSON valido o tiene un valor no permitido",
                request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<PersonErrorDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST.value(),
                "El parametro '" + ex.getName() + "' tiene un valor invalido", request);
    }

    private ResponseEntity<PersonErrorDTO> build(int status, String message,
                                                 HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(PersonErrorDTO.of(status, message, request.getRequestURI()));
    }
}