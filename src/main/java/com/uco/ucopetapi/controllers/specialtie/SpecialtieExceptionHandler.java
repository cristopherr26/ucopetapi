package com.uco.ucopetapi.controllers.specialtie;

import com.uco.ucopetapi.controllers.specialtieDoctor.SpecialtieDoctorController;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.service.specialtie.exception.SpecialtieConflictException;
import com.uco.ucopetapi.service.specialtie.exception.SpecialtieNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice(assignableTypes = {SpecialtieController.class, SpecialtieDoctorController.class})
public class SpecialtieExceptionHandler {

    private static final String MESSAGE_KEY = "message";

    @ExceptionHandler(SpecialtieNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(final SpecialtieNotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SpecialtieConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(final SpecialtieConflictException ex) {
        return body(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(final DataIntegrityViolationException ex) {
        return body(HttpStatus.CONFLICT, "La operación entra en conflicto con datos ya registrados.");
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusiness(final BusinessException ex) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(final MethodArgumentTypeMismatchException ex) {
        return body(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' tiene un formato inválido.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParameter(final MissingServletRequestParameterException ex) {
        return body(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getParameterName() + "' es obligatorio.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleNotReadable(final HttpMessageNotReadableException ex) {
        return body(HttpStatus.BAD_REQUEST, "El cuerpo de la petición es inválido o tiene un formato incorrecto.");
    }

    private ResponseEntity<Map<String, String>> body(final HttpStatus status, final String message) {
        return ResponseEntity.status(status).body(Map.of(MESSAGE_KEY, message == null ? "" : message));
    }
}
