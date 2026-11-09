package com.orlo.api.client;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Envía la notificación de lección nueva al tema de FCM. En desarrollo (emulador, sin proyecto real)
 * está apagado con app.fcm.enabled=false y solo deja en el log lo que enviaría.
 */
@Component
public class FirebasePushNotificationClient implements PushNotificationClient {

    private static final Logger log = LoggerFactory.getLogger(FirebasePushNotificationClient.class);

    private final FirebaseApp firebaseApp;
    private final boolean enabled;
    private final String lessonsTopic;

    public FirebasePushNotificationClient(FirebaseApp firebaseApp,
                                          @Value("${app.fcm.enabled:false}") boolean enabled,
                                          @Value("${app.fcm.topic-lessons}") String lessonsTopic) {
        this.firebaseApp = firebaseApp;
        this.enabled = enabled;
        this.lessonsTopic = lessonsTopic;
    }

    @Override
    public void lessonPublished(Long lessonId, String title) {
        if (!enabled) {
            log.info("FCM apagado: se enviaría al tema '{}' la lección {} ({})", lessonsTopic, lessonId, title);
            return;
        }
        Message message = Message.builder()
                .setTopic(lessonsTopic)
                .putData("lessonId", String.valueOf(lessonId))
                .putData("title", title)
                .setNotification(Notification.builder()
                        .setTitle("Nueva lección en ORLO")
                        .setBody(title)
                        .build())
                .build();
        try {
            String id = FirebaseMessaging.getInstance(firebaseApp).send(message);
            log.info("Notificación de la lección {} enviada ({})", lessonId, id);
        } catch (FirebaseMessagingException e) {
            log.error("No se pudo enviar la notificación de la lección {}", lessonId, e);
        }
    }
}
