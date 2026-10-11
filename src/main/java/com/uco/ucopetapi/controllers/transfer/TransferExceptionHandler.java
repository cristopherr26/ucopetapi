package com.uco.ucopetapi.controllers.transfer;

import com.uco.ucopetapi.dto.transfers.ApiErrorDTO;
import com.uco.ucopetapi.service.transfer.exception.InvalidTransferRequestException;
import com.uco.ucopetapi.service.transfer.exception.InvalidTransferStateException;
import com.uco.ucopetapi.service.transfer.exception.TransferNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.uco.ucopetapi.controllers.transfer")
public class TransferExceptionHandler {

    @ExceptionHandler(TransferNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleNotFound(TransferNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidTransferStateException.class)
    public ResponseEntity<ApiErrorDTO> handleInvalidState(InvalidTransferStateException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidTransferRequestException.class)
    public ResponseEntity<ApiErrorDTO> handleInvalidRequest(InvalidTransferRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<ApiErrorDTO> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiErrorDTO.of(status.value(), message));
    }
}