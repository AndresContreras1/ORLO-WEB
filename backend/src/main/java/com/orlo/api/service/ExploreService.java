package com.orlo.api.service;

import com.orlo.api.dto.CompleteWardrobeRequest;
import com.orlo.api.dto.ExploreItemDetailDto;
import com.orlo.api.dto.ExploreItemDto;
import com.orlo.api.dto.ExploreSection;
import com.orlo.api.dto.WardrobeSuggestionDto;
import com.orlo.api.dto.admin.AdminExploreItemDto;
import com.orlo.api.dto.admin.ExploreItemRequest;

import java.util.List;

/** Explora: inspiración conectada al perfil. Enseña a aprovechar lo que ya se tiene; comprar es opcional. */
public interface ExploreService {

    List<ExploreItemDto> items(ExploreSection section, String style, boolean premium);

    ExploreItemDetailDto item(Long id, boolean premium);

    /** Tipos de prenda que darían más combinaciones con lo que la persona ya tiene. */
    List<WardrobeSuggestionDto> completeWardrobe(CompleteWardrobeRequest request, boolean premium);

    List<AdminExploreItemDto> findAll();

    AdminExploreItemDto findById(Long id);

    AdminExploreItemDto create(ExploreItemRequest request);

    AdminExploreItemDto update(Long id, ExploreItemRequest request);

    void delete(Long id);
}
