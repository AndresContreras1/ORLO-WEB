package com.orlo.api.security;

/**
 * Usuario de la app autenticado con un ID token de Firebase.
 * Se obtiene en los controladores con {@code @AuthenticationPrincipal}.
 */
public record AppPrincipal(String uid, String email) {

    public static final String AUTHORITY = "APP_USER";
}
