package com.orlo.api.dto.admin;

import com.orlo.api.dto.Urls;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record OptionRequest(
        @NotBlank(message = "El texto de la opción es obligatorio") @Size(max = 200) String text,
        @Size(max = 500) @Pattern(regexp = Urls.WEB_OR_EMPTY_REGEX, message = Urls.MESSAGE) String imageUrl,
        @NotNull(message = "Falta la tabla de puntos")
        Map<@NotNull Long, @NotNull @Min(value = 0, message = "Mínimo 0 puntos") @Max(value = 10, message = "Máximo 10 puntos") Integer> scores) {
}
