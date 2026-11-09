package com.orlo.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdvisorMessageRequest(
        @NotBlank(message = "Escribe tu pregunta") @Size(max = 1000, message = "La pregunta es muy larga") String message,
        QuickAction quickAction,
        @NotNull(message = "Falta el perfil") @Valid AdvisorProfile profile,
        @Size(max = 80) List<@Valid WardrobeItem> wardrobe,
        @Size(max = 20) List<@Valid HistoryItem> history) {

    public List<WardrobeItem> wardrobeOrEmpty() {
        return wardrobe == null ? List.of() : wardrobe;
    }

    public List<HistoryItem> historyOrEmpty() {
        return history == null ? List.of() : history;
    }
}
