package com.orlo.api.exception;

import org.springframework.http.HttpStatus;

/** El servicio de IA no respondió o no está configurado. El mensaje no se cobra al usuario. */
public class AiUnavailableException extends ApiException {

    public AiUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE", message);
    }
}
