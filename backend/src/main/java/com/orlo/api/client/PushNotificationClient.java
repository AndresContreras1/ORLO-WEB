package com.orlo.api.client;

/** Notificaciones push a la app (Firebase Cloud Messaging). */
public interface PushNotificationClient {

    /** Avisa al tema de lecciones que hay una lección nueva. Nunca lanza: si falla, solo se registra. */
    void lessonPublished(Long lessonId, String title);
}
