package com.orlo.api.dto.admin;

import com.orlo.api.dto.CombineIdeaDto;
import com.orlo.api.entities.CatalogCodes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ExploreItemRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String name,
        @NotBlank(message = "La imagen es obligatoria") @Size(max = 500) String imageUrl,
        @Size(max = 200) String imageCredit,
        @NotBlank @Pattern(regexp = CatalogCodes.CATEGORY_REGEX, message = "Categoría no válida") String category,
        @NotBlank @Pattern(regexp = CatalogCodes.COLOR_REGEX, message = "Color no válido") String color,
        @NotEmpty(message = "Elige al menos un estilo") List<@NotBlank @Size(max = 40) String> styles,
        @NotBlank(message = "Explica por qué encaja") @Size(max = 500) String whyItFits,
        boolean trend,
        boolean accessory,
        boolean active,
        @Size(max = 3, message = "Máximo una idea por contexto") List<@Valid CombineIdeaDto> combineIdeas) {
}
