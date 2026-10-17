package com.uco.ucopetapi.service.procedure.exception;

import com.uco.ucopetapi.exception.BusinessException;

public class InvalidProcedureRequestException extends BusinessException {
    public InvalidProcedureRequestException(String message) {
        super(message);
    }
}