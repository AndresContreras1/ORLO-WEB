package com.orlo.api.service;

/** Una lección pasó a estar publicada. Se avisa a la app cuando la transacción ya se guardó. */
public record LessonPublishedEvent(Long lessonId, String title) {
}
