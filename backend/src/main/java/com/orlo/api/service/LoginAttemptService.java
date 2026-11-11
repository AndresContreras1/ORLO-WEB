package com.orlo.api.service;

/**
 * Frena la fuerza bruta en el login del panel: limita los intentos por IP y bloquea la cuenta
 * un rato después de varios fallos seguidos. El bloqueo siempre es temporal.
 */
public interface LoginAttemptService {

    /** Lanza TooManyAttemptsException si la IP o la cuenta pasaron el límite. */
    void checkAllowed(String email, String ip);

    void recordFailure(String email, String ip);

    /** Un login correcto reinicia los fallos de la cuenta. */
    void recordSuccess(String email);
}
