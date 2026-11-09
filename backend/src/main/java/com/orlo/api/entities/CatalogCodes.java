package com.orlo.api.entities;

import java.util.List;

/**
 * Códigos compartidos con la app y la web (los mismos que se guardan en Firestore).
 * Si cambian aquí, cambian en docs/modelo-de-datos.md y en los dos clientes.
 */
public final class CatalogCodes {

    public static final List<String> CATEGORIES = List.of("arriba", "abajo", "abrigo", "calzado", "accesorio", "otro");

    public static final List<String> COLORS = List.of("negro", "blanco", "gris", "beige", "cafe", "azul", "celeste",
            "verde", "rojo", "rosado", "amarillo", "morado", "estampado");

    /** Contextos de "Cómo combinarla". */
    public static final List<String> CONTEXTS = List.of("casual", "universidad", "elegante");

    public static final String CATEGORY_REGEX = "arriba|abajo|abrigo|calzado|accesorio|otro";
    public static final String COLOR_REGEX = "negro|blanco|gris|beige|cafe|azul|celeste|verde|rojo|rosado|amarillo|morado|estampado";

    private CatalogCodes() {
    }
}
