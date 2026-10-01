package com.uco.ucopetapi.service.transfer.exception;

import com.uco.ucopetapi.exception.BusinessException;

public class InvalidTransferStateException extends BusinessException {
    public InvalidTransferStateException(String message) {
        super(message);
    }
}