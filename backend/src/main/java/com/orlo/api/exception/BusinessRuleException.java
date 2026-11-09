package com.orlo.api.exception;

import org.springframework.http.HttpStatus;

/** Regla de negocio violada (duplicados, datos inconsistentes): 409. */
public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String message) {
        super(HttpStatus.CONFLICT, "BUSINESS_RULE", message);
    }
}
