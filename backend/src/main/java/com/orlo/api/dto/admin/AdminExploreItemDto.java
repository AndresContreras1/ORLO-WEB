package com.orlo.api.dto.admin;

import com.orlo.api.dto.CombineIdeaDto;
import com.orlo.api.entities.ExploreItem;

import java.util.List;

public record AdminExploreItemDto(Long id, String name, String imageUrl, String imageCredit, String category,
                                  String color, List<String> styles, String whyItFits, boolean trend,
                                  boolean accessory, boolean active, List<CombineIdeaDto> combineIdeas) {

    public static AdminExploreItemDto from(ExploreItem item) {
        return new AdminExploreItemDto(item.getId(), item.getName(), item.getImageUrl(), item.getImageCredit(),
                item.getCategory(), item.getColor(), item.getStyles().stream().sorted().toList(), item.getWhyItFits(),
                item.isTrend(), item.isAccessory(), item.isActive(),
                item.getCombineIdeas().stream().map(CombineIdeaDto::from).toList());
    }
}
