package com.orlo.api.service;

import com.orlo.api.dto.LessonDetailDto;
import com.orlo.api.entities.Lesson;
import com.orlo.api.exception.NotFoundException;
import com.orlo.api.exception.PremiumRequiredException;
import com.orlo.api.repository.LessonRepository;
import com.orlo.api.service.impl.LessonServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonServiceImplTest {

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private LessonServiceImpl lessonService;

    @Test
    void detail_freeSampleAndFreeUser_returnsLesson() {
        when(lessonRepository.findByIdAndPublishedTrue(1L)).thenReturn(Optional.of(lesson(1L, true)));

        LessonDetailDto detail = lessonService.detail(1L, false);

        assertThat(detail.freeSample()).isTrue();
        assertThat(detail.review()).isNotNull();
        assertThat(detail.review().correctIndex()).isEqualTo(1);
    }

    @Test
    void detail_premiumLessonAndFreeUser_throwsPremiumRequired() {
        when(lessonRepository.findByIdAndPublishedTrue(2L)).thenReturn(Optional.of(lesson(2L, false)));

        assertThatThrownBy(() -> lessonService.detail(2L, false))
                .isInstanceOf(PremiumRequiredException.class)
                .hasFieldOrPropertyWithValue("code", "PREMIUM_REQUIRED");
    }

    @Test
    void detail_premiumLessonAndPremiumUser_returnsLesson() {
        when(lessonRepository.findByIdAndPublishedTrue(2L)).thenReturn(Optional.of(lesson(2L, false)));

        assertThat(lessonService.detail(2L, true).id()).isEqualTo(2L);
    }

    @Test
    void detail_unpublishedOrMissing_throwsNotFound() {
        when(lessonRepository.findByIdAndPublishedTrue(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.detail(9L, true)).isInstanceOf(NotFoundException.class);
    }

    private static Lesson lesson(Long id, boolean free) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setTitle("Lección " + id);
        lesson.setSummary("Resumen");
        lesson.setMainIdea("Idea");
        lesson.setTip("Consejo");
        lesson.setFreeSample(free);
        lesson.setReviewQuestion("¿Pregunta?");
        lesson.setReviewOptions(new java.util.ArrayList<>(List.of("A", "B")));
        lesson.setReviewCorrectIndex(1);
        return lesson;
    }
}
