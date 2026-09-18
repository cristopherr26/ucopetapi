package com.uco.ucopetapi.controllers.purchases.exception;

import com.uco.ucopetapi.service.purchases.exception.DuplicatePurchaseNumberException;
import com.uco.ucopetapi.service.purchases.exception.HeadquarterInactiveException;
import com.uco.ucopetapi.service.purchases.exception.HeadquarterNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.ProductInactiveException;
import com.uco.ucopetapi.service.purchases.exception.ProductNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.PurchaseNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.PurchaseNumberNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.ServiceItemNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.ServiceItemNotPurchasableException;
import com.uco.ucopetapi.service.purchases.exception.SupplierNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.uco.ucopetapi.controllers.purchases")
public class PurchaseExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(PurchaseExceptionHandler.class);

    @ExceptionHandler(PurchaseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePurchaseNotFound(PurchaseNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PurchaseNumberNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePurchaseNumberNotFound(PurchaseNumberNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SupplierNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSupplierNotFound(SupplierNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(HeadquarterNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleHeadquarterNotFound(HeadquarterNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(HeadquarterInactiveException.class)
    public ResponseEntity<Map<String, Object>> handleHeadquarterInactive(HeadquarterInactiveException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotFound(ProductNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ProductInactiveException.class)
    public ResponseEntity<Map<String, Object>> handleProductInactive(ProductInactiveException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DuplicatePurchaseNumberException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicatePurchaseNumber(DuplicatePurchaseNumberException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ServiceItemNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleServiceItemNotFound(ServiceItemNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ServiceItemNotPurchasableException.class)
    public ResponseEntity<Map<String, Object>> handleServiceItemNotPurchasable(ServiceItemNotPurchasableException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud contiene datos inválidos o mal formados.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        log.error("Error inesperado procesando una solicitud de compras", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado procesando la solicitud.");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now(ZoneId.of("America/Bogota")));
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return new ResponseEntity<>(response, status);
    }
}
