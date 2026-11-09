package com.orlo.api.dto.admin;

import com.orlo.api.entities.QuizQuestion;

import java.util.List;

public record AdminQuestionDto(Long id, String text, String helpText, boolean multiple, int order, boolean active,
                               List<AdminOptionDto> options) {

    public static AdminQuestionDto from(QuizQuestion question) {
        return new AdminQuestionDto(question.getId(), question.getText(), question.getHelpText(), question.isMultiple(),
                question.getPosition(), question.isActive(),
                question.getOptions().stream().map(AdminOptionDto::from).toList());
    }
}
