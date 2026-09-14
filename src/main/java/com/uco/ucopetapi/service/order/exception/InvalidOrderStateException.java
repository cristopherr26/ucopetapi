package com.uco.ucopetapi.service.order.exception;

import com.uco.ucopetapi.exception.BusinessException;

public class InvalidOrderStateException extends BusinessException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}