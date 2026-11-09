package com.orlo.api.dto;

import com.orlo.api.entities.Style;

import java.util.List;

public record StyleDto(Long id, String code, String name, String description,
                       List<String> palette, List<String> gallery) {

    /** @param galleryLimit cuántas imágenes de la galería se entregan (la versión gratis es corta) */
    public static StyleDto from(Style style, int galleryLimit) {
        List<String> gallery = style.getGallery().stream().limit(galleryLimit).toList();
        return new StyleDto(style.getId(), style.getCode(), style.getName(), style.getDescription(),
                List.copyOf(style.getPalette()), gallery);
    }

    public static StyleDto from(Style style) {
        return from(style, Integer.MAX_VALUE);
    }
}
