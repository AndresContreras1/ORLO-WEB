package com.orlo.api.dto;

import com.orlo.api.entities.ExploreItem;

import java.util.List;

/** Prenda del catálogo de Explora. {@code imageCredit}: autor y licencia de la foto. */
public record ExploreItemDto(Long id, String name, String imageUrl, String imageCredit, String category, String color,
                             List<String> styles, String whyItFits, boolean trend, boolean accessory) {

    public static ExploreItemDto from(ExploreItem item) {
        return new ExploreItemDto(item.getId(), item.getName(), item.getImageUrl(), item.getImageCredit(),
                item.getCategory(), item.getColor(), item.getStyles().stream().sorted().toList(), item.getWhyItFits(),
                item.isTrend(), item.isAccessory());
    }
}
