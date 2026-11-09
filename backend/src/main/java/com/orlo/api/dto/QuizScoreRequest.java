package com.orlo.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record QuizScoreRequest(
        @NotEmpty(message = "Responde al menos una pregunta") List<@Valid QuizAnswerRequest> answers) {
}
