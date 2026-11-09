package com.orlo.api.dto.admin;

import com.orlo.api.dto.LessonExampleDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record LessonRequest(
        @NotBlank(message = "El título es obligatorio") @Size(max = 120) String title,
        @NotBlank(message = "El resumen es obligatorio") @Size(max = 300) String summary,
        @NotBlank(message = "La idea principal es obligatoria") @Size(max = 2000) String mainIdea,
        @NotBlank(message = "El consejo es obligatorio") @Size(max = 600) String tip,
        @Min(value = 1, message = "El orden empieza en 1") int order,
        @Min(value = 1, message = "Mínimo 1 minuto") @Max(value = 15, message = "Máximo 15 minutos") int minutes,
        @Size(max = 500) String coverUrl,
        boolean freeSample,
        boolean usesColorProfile,
        boolean published,
        @Size(max = 300) String reviewQuestion,
        @Size(max = 4) List<@NotBlank @Size(max = 200) String> reviewOptions,
        Integer reviewCorrectIndex,
        @Size(max = 5, message = "Máximo 5 ejemplos") List<@Valid LessonExampleDto> examples) {
}
