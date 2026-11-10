package com.orlo.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuizScoreRequest(
        @NotEmpty(message = "Responde al menos una pregunta")
        @Size(max = 50, message = "Demasiadas respuestas") List<@Valid QuizAnswerRequest> answers) {
}
