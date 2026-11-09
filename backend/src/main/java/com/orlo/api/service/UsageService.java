package com.orlo.api.service;

/** Límite diario de mensajes del asesor y registro de tokens y costo. */
public interface UsageService {

    int remainingToday(String uid, int dailyLimit);

    /**
     * Descuenta un mensaje antes de llamar al modelo. Lanza LimitReachedException si ya no quedan.
     *
     * @return mensajes que quedan después de este
     */
    int reserve(String uid, int dailyLimit);

    /** Devuelve el mensaje reservado cuando el modelo falla: al usuario no se le cobra. */
    void release(String uid);

    /** Cuenta un outfit del día. Lanza LimitReachedException si ya se pidieron todos los de hoy. */
    void reserveDailyOutfit(String uid, int dailyLimit);

    void recordTokens(String uid, String model, long inputTokens, long outputTokens);
}
