package com.orlo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HistoryItem(@NotNull ChatRole role, @NotBlank @Size(max = 4000) String text) {
}
