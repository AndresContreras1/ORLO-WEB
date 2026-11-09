package com.orlo.api.exception;

import org.springframework.http.HttpStatus;

/** El usuario llegó al límite diario de mensajes del asesor. */
public class LimitReachedException extends ApiException {

    public LimitReachedException(int dailyLimit) {
        super(HttpStatus.TOO_MANY_REQUESTS, "LIMIT_REACHED",
                "Llegaste al límite de " + dailyLimit + " mensajes de hoy. Vuelve mañana");
    }
}
