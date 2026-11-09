package com.orlo.api.client;

import java.util.Optional;

/**
 * Acceso a Firebase Authentication. Es una interfaz para poder reemplazarla en las pruebas
 * sin depender del emulador.
 */
public interface FirebaseAuthClient {

    /** Valida el ID token de la app. Vacío si el token no es válido o venció. */
    Optional<FirebaseIdentity> verify(String idToken);

    /** Borra la cuenta de Firebase Authentication (derecho de supresión). */
    void deleteUser(String uid);

    record FirebaseIdentity(String uid, String email) {
    }
}
