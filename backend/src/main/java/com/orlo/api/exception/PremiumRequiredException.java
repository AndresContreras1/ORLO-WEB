package com.orlo.api.exception;

import org.springframework.http.HttpStatus;

/** El usuario intenta usar una función premium sin serlo: la app muestra la oferta. */
public class PremiumRequiredException extends ApiException {

    public PremiumRequiredException() {
        super(HttpStatus.FORBIDDEN, "PREMIUM_REQUIRED", "Esta función es parte de ORLO Premium");
    }
}
