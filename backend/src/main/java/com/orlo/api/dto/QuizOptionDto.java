package com.orlo.api.dto;

import com.orlo.api.entities.QuizOption;

public record QuizOptionDto(Long id, String text, String imageUrl) {

    public static QuizOptionDto from(QuizOption option) {
        return new QuizOptionDto(option.getId(), option.getText(), option.getImageUrl());
    }
}
