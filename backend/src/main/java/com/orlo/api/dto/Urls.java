package com.orlo.api.dto;

/**
 * Validación de los enlaces que llegan del panel (imágenes, logos, portadas). Solo http y https:
 * así no se guardan javascript:, data: ni rutas que el navegador pueda interpretar de otra forma.
 */
public final class Urls {

    public static final String WEB_REGEX = "https?://\\S+";
    /** Para campos opcionales: el formulario manda texto vacío cuando no hay enlace. */
    public static final String WEB_OR_EMPTY_REGEX = "(https?://\\S+)?";
    public static final String MESSAGE = "Usa un enlace que empiece por http:// o https://";

    private Urls() {
    }
}
