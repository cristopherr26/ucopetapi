package com.uco.ucopetapi.service.transfer.exception;

public class InvalidTransferStateException extends RuntimeException {
    public InvalidTransferStateException(String message) {
        super(message);
    }
}
