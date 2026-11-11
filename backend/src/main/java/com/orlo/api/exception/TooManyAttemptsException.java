package com.orlo.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Demasiadas peticiones o intentos de login seguidos. Responde 429 con Retry-After.
 * El mensaje no dice si la cuenta existe, si quedó bloqueada ni cuántos intentos quedan.
 */
@Getter
public class TooManyAttemptsException extends ApiException {

    private final long retryAfterSeconds;

    public TooManyAttemptsException(long retryAfterSeconds) {
        super(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS",
                "Hubo muchos intentos seguidos. Espera unos minutos y vuelve a intentar");
        this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
    }
}
