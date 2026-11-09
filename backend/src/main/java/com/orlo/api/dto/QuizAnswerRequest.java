package com.orlo.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record QuizAnswerRequest(
        @NotNull(message = "Falta la pregunta") Long questionId,
        @NotEmpty(message = "Elige al menos una opción") List<@NotNull Long> optionIds) {
}
