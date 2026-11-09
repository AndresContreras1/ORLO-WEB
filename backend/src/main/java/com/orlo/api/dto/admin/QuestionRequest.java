package com.orlo.api.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionRequest(
        @NotBlank(message = "La pregunta es obligatoria") @Size(max = 300) String text,
        @Size(max = 300) String helpText,
        boolean multiple,
        @Min(value = 1, message = "El orden empieza en 1") int order,
        boolean active,
        @Size(min = 2, max = 8, message = "Cada pregunta lleva entre 2 y 8 opciones") List<@Valid OptionRequest> options) {
}
