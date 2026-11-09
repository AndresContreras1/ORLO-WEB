package com.orlo.api.dto;

import com.orlo.api.entities.LessonExample;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LessonExampleDto(
        @NotBlank(message = "La imagen es obligatoria") @Size(max = 500) String imageUrl,
        @NotBlank(message = "El texto es obligatorio") @Size(max = 500) String text) {

    public static LessonExampleDto from(LessonExample example) {
        return new LessonExampleDto(example.getImageUrl(), example.getText());
    }
}
