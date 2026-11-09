package com.orlo.api.dto;

import com.orlo.api.entities.Lesson;

import java.util.List;

/** Pregunta de repaso opcional de una lección. */
public record LessonReviewDto(String question, List<String> options, int correctIndex) {

    /** Devuelve null cuando la lección no tiene pregunta de repaso completa. */
    public static LessonReviewDto from(Lesson lesson) {
        if (lesson.getReviewQuestion() == null || lesson.getReviewQuestion().isBlank()
                || lesson.getReviewOptions().isEmpty() || lesson.getReviewCorrectIndex() == null) {
            return null;
        }
        return new LessonReviewDto(lesson.getReviewQuestion(), List.copyOf(lesson.getReviewOptions()),
                lesson.getReviewCorrectIndex());
    }
}
