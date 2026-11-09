package com.orlo.api.dto.admin;

import com.orlo.api.entities.OptionStyleScore;
import com.orlo.api.entities.QuizOption;

import java.util.Map;
import java.util.TreeMap;

/** Opción con su fila de la tabla de puntos: id de estilo → puntos. */
public record AdminOptionDto(Long id, String text, String imageUrl, int order, Map<Long, Integer> scores) {

    public static AdminOptionDto from(QuizOption option) {
        Map<Long, Integer> scores = new TreeMap<>();
        for (OptionStyleScore score : option.getScores()) {
            scores.put(score.getStyle().getId(), score.getPoints());
        }
        return new AdminOptionDto(option.getId(), option.getText(), option.getImageUrl(), option.getPosition(), scores);
    }
}
