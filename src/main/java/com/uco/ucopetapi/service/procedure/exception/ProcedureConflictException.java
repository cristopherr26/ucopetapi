package com.uco.ucopetapi.service.procedure.exception;

import com.uco.ucopetapi.exception.BusinessException;

public class ProcedureConflictException extends BusinessException {
    public ProcedureConflictException(String message) {
        super(message);
    }
}