package com.orlo.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DailyOutfitRequest(
        @NotNull(message = "Falta el perfil") @Valid AdvisorProfile profile,
        @Size(max = 80) List<@Valid WardrobeItem> wardrobe) {

    public List<WardrobeItem> wardrobeOrEmpty() {
        return wardrobe == null ? List.of() : wardrobe;
    }
}
