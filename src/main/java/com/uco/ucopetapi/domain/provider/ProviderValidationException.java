package com.uco.ucopetapi.domain.provider;

import com.uco.ucopetapi.exception.BusinessException;

public class ProviderValidationException extends BusinessException {

    public ProviderValidationException(String message) {
        super(message);
    }
}
