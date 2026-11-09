package com.orlo.api.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StoreRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String name,
        @Size(max = 500) String logoUrl,
        @NotBlank(message = "Indica qué tipo de prendas vende") @Size(max = 200) String garmentTypes,
        @NotBlank(message = "El enlace es obligatorio")
        @Pattern(regexp = "https?://.+", message = "El enlace debe empezar por http:// o https://")
        @Size(max = 500) String url,
        boolean active,
        boolean affiliate) {
}
