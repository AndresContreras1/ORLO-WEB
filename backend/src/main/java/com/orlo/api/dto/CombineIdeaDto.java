package com.orlo.api.dto;

import com.orlo.api.entities.CombineIdea;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CombineIdeaDto(
        @Pattern(regexp = "casual|universidad|elegante", message = "El contexto es casual, universidad o elegante") String context,
        @NotEmpty(message = "Agrega al menos una prenda a la idea") List<@Size(min = 1, max = 80) String> items) {

    public static CombineIdeaDto from(CombineIdea idea) {
        return new CombineIdeaDto(idea.getContext(), idea.itemList());
    }
}
