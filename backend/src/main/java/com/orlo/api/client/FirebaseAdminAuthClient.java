package com.orlo.api.client;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Implementación con el SDK de Firebase Admin. En desarrollo apunta al emulador de Auth. */
@Component
public class FirebaseAdminAuthClient implements FirebaseAuthClient {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAdminAuthClient.class);

    private final FirebaseApp firebaseApp;

    public FirebaseAdminAuthClient(FirebaseApp firebaseApp) {
        this.firebaseApp = firebaseApp;
    }

    @Override
    public Optional<FirebaseIdentity> verify(String idToken) {
        try {
            FirebaseToken token = FirebaseAuth.getInstance(firebaseApp).verifyIdToken(idToken);
            return Optional.of(new FirebaseIdentity(token.getUid(), token.getEmail()));
        } catch (FirebaseAuthException | IllegalArgumentException e) {
            log.debug("Token de Firebase rechazado: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void deleteUser(String uid) {
        try {
            FirebaseAuth.getInstance(firebaseApp).deleteUser(uid);
        } catch (FirebaseAuthException e) {
            if (e.getAuthErrorCode() == AuthErrorCode.USER_NOT_FOUND) {
                log.info("La cuenta {} ya no existía en Firebase", uid);
                return;
            }
            log.error("No se pudo borrar la cuenta {} de Firebase", uid, e);
            throw new IllegalStateException("No se pudo borrar la cuenta de Firebase", e);
        }
    }
}
