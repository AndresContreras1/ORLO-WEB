package com.orlo.api.dto.admin;

import com.orlo.api.entities.AiConfig;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Instrucciones y límites del asesor IA. Se usa para leer y para guardar. */
public record AiConfigDto(
        @NotBlank(message = "Las instrucciones son obligatorias") @Size(max = 8000) String systemPrompt,
        @NotBlank(message = "El modelo es obligatorio") @Size(max = 60) String model,
        @Min(value = 1, message = "Mínimo 1 mensaje al día") @Max(value = 500) int dailyLimit,
        @Min(value = 300, message = "Mínimo 300 tokens") @Max(value = 4000) int maxOutputTokens) {

    public static AiConfigDto from(AiConfig config) {
        return new AiConfigDto(config.getSystemPrompt(), config.getModel(), config.getDailyLimit(),
                config.getMaxOutputTokens());
    }
}
