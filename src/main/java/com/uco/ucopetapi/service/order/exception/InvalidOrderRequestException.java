package com.uco.ucopetapi.service.order.exception;

import com.uco.ucopetapi.exception.BusinessException;

public class InvalidOrderRequestException extends BusinessException {
    public InvalidOrderRequestException(String message) {
        super(message);
    }
}