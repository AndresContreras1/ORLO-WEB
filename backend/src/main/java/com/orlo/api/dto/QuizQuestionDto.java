package com.orlo.api.dto;

import com.orlo.api.entities.QuizQuestion;

import java.util.List;

public record QuizQuestionDto(Long id, String text, String helpText, boolean multiple, int order,
                              List<QuizOptionDto> options) {

    public static QuizQuestionDto from(QuizQuestion question) {
        return new QuizQuestionDto(question.getId(), question.getText(), question.getHelpText(),
                question.isMultiple(), question.getPosition(),
                question.getOptions().stream().map(QuizOptionDto::from).toList());
    }
}
