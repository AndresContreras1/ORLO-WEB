package com.orlo.api.dto.admin;

import com.orlo.api.dto.Urls;
import com.orlo.api.entities.CatalogCodes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StoreRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String name,
        @Size(max = 500) @Pattern(regexp = Urls.WEB_OR_EMPTY_REGEX, message = Urls.MESSAGE) String logoUrl,
        @NotBlank(message = "Indica qué tipo de prendas vende") @Size(max = 200) String garmentTypes,
        @NotBlank(message = "El enlace es obligatorio")
        @Pattern(regexp = "https?://.+", message = "El enlace debe empezar por http:// o https://")
        @Size(max = 500) String url,
        boolean active,
        boolean affiliate,
        List<@Pattern(regexp = CatalogCodes.CATEGORY_REGEX, message = "Categoría no válida") String> categories) {

    public List<String> categoriesOrEmpty() {
        return categories == null ? List.of() : categories;
    }
}
