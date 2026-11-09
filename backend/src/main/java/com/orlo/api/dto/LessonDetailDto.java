package com.orlo.api.dto;

import com.orlo.api.entities.Lesson;

import java.util.List;

public record LessonDetailDto(Long id, String title, String mainIdea, List<LessonExampleDto> examples, String tip,
                              LessonReviewDto review, boolean freeSample, boolean usesColorProfile) {

    public static LessonDetailDto from(Lesson lesson) {
        return new LessonDetailDto(lesson.getId(), lesson.getTitle(), lesson.getMainIdea(),
                lesson.getExamples().stream().map(LessonExampleDto::from).toList(), lesson.getTip(),
                LessonReviewDto.from(lesson), lesson.isFreeSample(), lesson.isUsesColorProfile());
    }
}
