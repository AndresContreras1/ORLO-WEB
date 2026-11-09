package com.orlo.api.dto;

import com.orlo.api.entities.ExploreItem;

import java.util.List;

/** Detalle de una prenda de Explora: lo mismo que la lista más las ideas para combinarla. */
public record ExploreItemDetailDto(Long id, String name, String imageUrl, String imageCredit, String category,
                                   String color, List<String> styles, String whyItFits, boolean trend,
                                   boolean accessory, List<CombineIdeaDto> combineIdeas) {

    public static ExploreItemDetailDto from(ExploreItem item) {
        ExploreItemDto base = ExploreItemDto.from(item);
        return new ExploreItemDetailDto(base.id(), base.name(), base.imageUrl(), base.imageCredit(), base.category(),
                base.color(), base.styles(), base.whyItFits(), base.trend(), base.accessory(),
                item.getCombineIdeas().stream().map(CombineIdeaDto::from).toList());
    }
}
