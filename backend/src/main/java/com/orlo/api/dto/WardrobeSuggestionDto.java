package com.orlo.api.dto;

import java.util.List;

/** Un tipo de prenda que daría más combinaciones con lo que la persona ya tiene. */
public record WardrobeSuggestionDto(String category, String title, String reason, List<ExploreItemDto> items) {
}
