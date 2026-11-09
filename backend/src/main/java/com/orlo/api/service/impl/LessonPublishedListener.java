package com.orlo.api.service.impl;

import com.orlo.api.client.PushNotificationClient;
import com.orlo.api.service.LessonPublishedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * La notificación sale después del commit: si guardar la lección falla, no se avisa nada, y la
 * llamada a Firebase no queda dentro de la transacción.
 */
@Component
public class LessonPublishedListener {

    private final PushNotificationClient pushNotificationClient;

    public LessonPublishedListener(PushNotificationClient pushNotificationClient) {
        this.pushNotificationClient = pushNotificationClient;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLessonPublished(LessonPublishedEvent event) {
        pushNotificationClient.lessonPublished(event.lessonId(), event.title());
    }
}
