package com.uco.ucopetapi.service.order.exception;

import com.uco.ucopetapi.exception.BusinessException;

public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
