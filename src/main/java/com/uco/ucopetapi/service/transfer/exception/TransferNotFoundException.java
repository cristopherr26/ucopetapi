package com.uco.ucopetapi.service.transfer.exception;

import com.uco.ucopetapi.exception.BusinessException;

public class TransferNotFoundException extends BusinessException {
    public TransferNotFoundException(String message) {
        super(message);
    }
}