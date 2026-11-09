package com.orlo.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Lo que la app sabe del armario: solo categoría y color de cada prenda. */
public record CompleteWardrobeRequest(
        @Size(max = 60) String primaryStyle,
        @Size(max = 200) List<@Valid WardrobeItem> wardrobe) {

    public List<WardrobeItem> wardrobeOrEmpty() {
        return wardrobe == null ? List.of() : wardrobe;
    }
}
