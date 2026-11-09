package com.orlo.api.dto;

import java.util.Map;

public record QuizScoreResponse(StyleDto primary, StyleDto secondary, Map<String, Integer> scores) {
}
