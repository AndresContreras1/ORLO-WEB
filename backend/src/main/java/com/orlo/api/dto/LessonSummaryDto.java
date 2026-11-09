package com.orlo.api.dto;

import com.orlo.api.entities.Lesson;

public record LessonSummaryDto(Long id, String title, String summary, int order, boolean freeSample,
                               String coverUrl, int minutes) {

    public static LessonSummaryDto from(Lesson lesson) {
        return new LessonSummaryDto(lesson.getId(), lesson.getTitle(), lesson.getSummary(), lesson.getPosition(),
                lesson.isFreeSample(), lesson.getCoverUrl(), lesson.getMinutes());
    }
}
