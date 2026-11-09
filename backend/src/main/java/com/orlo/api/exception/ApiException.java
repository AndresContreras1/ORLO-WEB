package com.orlo.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Error de negocio con estado HTTP y código estable que la app y el panel pueden leer.
 * Extiende RuntimeException para que las transacciones hagan rollback.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    protected ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
