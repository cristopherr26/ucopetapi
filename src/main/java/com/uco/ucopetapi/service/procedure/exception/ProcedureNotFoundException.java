package com.uco.ucopetapi.service.procedure.exception;

import com.uco.ucopetapi.exception.BusinessException;

public class ProcedureNotFoundException extends BusinessException {
    public ProcedureNotFoundException(String message) {
        super(message);
    }
}