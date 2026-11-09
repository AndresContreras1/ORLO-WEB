package com.orlo.api.dto.admin;

import com.orlo.api.dto.LessonExampleDto;
import com.orlo.api.entities.Lesson;

import java.util.List;

public record AdminLessonDto(Long id, String title, String summary, String mainIdea, String tip, int order,
                             int minutes, String coverUrl, boolean freeSample, boolean usesColorProfile,
                             boolean published, String reviewQuestion, List<String> reviewOptions,
                             Integer reviewCorrectIndex, List<LessonExampleDto> examples) {

    public static AdminLessonDto from(Lesson lesson) {
        return new AdminLessonDto(lesson.getId(), lesson.getTitle(), lesson.getSummary(), lesson.getMainIdea(),
                lesson.getTip(), lesson.getPosition(), lesson.getMinutes(), lesson.getCoverUrl(), lesson.isFreeSample(),
                lesson.isUsesColorProfile(), lesson.isPublished(), lesson.getReviewQuestion(),
                List.copyOf(lesson.getReviewOptions()), lesson.getReviewCorrectIndex(),
                lesson.getExamples().stream().map(LessonExampleDto::from).toList());
    }
}
