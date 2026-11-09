package com.orlo.api.config;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.internal.FirebaseProcessEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Inicializa Firebase Admin. Con {@code app.firebase.auth-emulator-host} definido usa el emulador
 * local y credenciales de prueba; en producción usa las credenciales por defecto de Google.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);
    private static final String APP_NAME = "orlo-api";

    @Bean
    public FirebaseApp firebaseApp(@Value("${app.firebase.project-id}") String projectId,
                                   @Value("${app.firebase.auth-emulator-host:}") String emulatorHost) throws IOException {
        for (FirebaseApp app : FirebaseApp.getApps()) {
            if (APP_NAME.equals(app.getName())) {
                return app;
            }
        }
        GoogleCredentials credentials;
        if (emulatorHost != null && !emulatorHost.isBlank()) {
            FirebaseProcessEnvironment.setenv("FIREBASE_AUTH_EMULATOR_HOST", emulatorHost);
            credentials = GoogleCredentials.create(new AccessToken("owner", null));
            log.info("Firebase Auth apunta al emulador {} (proyecto {})", emulatorHost, projectId);
        } else {
            credentials = GoogleCredentials.getApplicationDefault();
        }
        FirebaseOptions options = FirebaseOptions.builder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build();
        return FirebaseApp.initializeApp(options, APP_NAME);
    }
}
