package com.orlo.api.dto.admin;

import com.orlo.api.dto.Urls;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StyleRequest(
        @NotBlank(message = "El código es obligatorio")
        @Pattern(regexp = "[a-z_]{2,40}", message = "Usa solo minúsculas y guion bajo (ej. urbano)") String code,
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String name,
        @NotBlank(message = "La descripción es obligatoria") @Size(max = 1000) String description,
        @NotEmpty(message = "Agrega al menos un color")
        List<@Pattern(regexp = "#[0-9A-Fa-f]{6}", message = "Cada color va en formato #RRGGBB") String> palette,
        @Size(max = 20, message = "Máximo 20 imágenes")
        List<@NotBlank @Size(max = 500) @Pattern(regexp = Urls.WEB_REGEX, message = Urls.MESSAGE) String> gallery) {
}
