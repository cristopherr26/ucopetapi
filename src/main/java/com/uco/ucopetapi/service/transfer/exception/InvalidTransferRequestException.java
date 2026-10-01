package com.uco.ucopetapi.service.transfer.exception;

import com.uco.ucopetapi.exception.BusinessException;

public class InvalidTransferRequestException extends BusinessException {
    public InvalidTransferRequestException(String message) {
        super(message);
    }
}