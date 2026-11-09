package com.orlo.api.dto;

import java.util.List;

/** Cuestionario activo. {@code version} cambia cada vez que el panel edita una pregunta. */
public record QuizDto(Long version, List<QuizQuestionDto> questions) {
}
