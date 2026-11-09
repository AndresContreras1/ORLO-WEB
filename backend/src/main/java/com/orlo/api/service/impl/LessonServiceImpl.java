package com.orlo.api.service.impl;

import com.orlo.api.dto.LessonDetailDto;
import com.orlo.api.dto.LessonExampleDto;
import com.orlo.api.dto.LessonSummaryDto;
import com.orlo.api.dto.admin.AdminLessonDto;
import com.orlo.api.dto.admin.LessonRequest;
import com.orlo.api.entities.Lesson;
import com.orlo.api.entities.LessonExample;
import com.orlo.api.exception.BusinessRuleException;
import com.orlo.api.exception.NotFoundException;
import com.orlo.api.exception.PremiumRequiredException;
import com.orlo.api.repository.LessonRepository;
import com.orlo.api.service.LessonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;

    public LessonServiceImpl(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonSummaryDto> listPublished() {
        return lessonRepository.findByPublishedTrueOrderByPositionAsc().stream().map(LessonSummaryDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LessonDetailDto detail(Long id, boolean premium) {
        Lesson lesson = lessonRepository.findByIdAndPublishedTrue(id)
                .orElseThrow(() -> new NotFoundException("Lección", id));
        if (!lesson.isFreeSample() && !premium) {
            throw new PremiumRequiredException();
        }
        return LessonDetailDto.from(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminLessonDto> findAll() {
        return lessonRepository.findAllByOrderByPositionAsc().stream().map(AdminLessonDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminLessonDto findById(Long id) {
        return AdminLessonDto.from(load(id));
    }

    @Override
    @Transactional
    public AdminLessonDto create(LessonRequest request) {
        Lesson lesson = new Lesson();
        apply(lesson, request);
        return AdminLessonDto.from(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public AdminLessonDto update(Long id, LessonRequest request) {
        Lesson lesson = load(id);
        apply(lesson, request);
        lessonRepository.flush();
        return AdminLessonDto.from(lesson);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        lessonRepository.delete(load(id));
    }

    private Lesson load(Long id) {
        return lessonRepository.findById(id).orElseThrow(() -> new NotFoundException("Lección", id));
    }

    private static void apply(Lesson lesson, LessonRequest request) {
        List<String> reviewOptions = request.reviewOptions() == null ? List.of() : request.reviewOptions();
        boolean hasReview = request.reviewQuestion() != null && !request.reviewQuestion().isBlank();
        if (hasReview && (reviewOptions.size() < 2 || request.reviewCorrectIndex() == null
                || request.reviewCorrectIndex() < 0 || request.reviewCorrectIndex() >= reviewOptions.size())) {
            throw new BusinessRuleException("La pregunta de repaso necesita al menos 2 opciones y la respuesta correcta");
        }
        lesson.setTitle(request.title().trim());
        lesson.setSummary(request.summary().trim());
        lesson.setMainIdea(request.mainIdea().trim());
        lesson.setTip(request.tip().trim());
        lesson.setPosition(request.order());
        lesson.setMinutes(request.minutes());
        lesson.setCoverUrl(request.coverUrl() == null || request.coverUrl().isBlank() ? null : request.coverUrl().trim());
        lesson.setFreeSample(request.freeSample());
        lesson.setUsesColorProfile(request.usesColorProfile());
        lesson.setPublished(request.published());
        lesson.setReviewQuestion(hasReview ? request.reviewQuestion().trim() : null);
        lesson.setReviewOptions(new ArrayList<>(hasReview ? reviewOptions : List.of()));
        lesson.setReviewCorrectIndex(hasReview ? request.reviewCorrectIndex() : null);
        List<LessonExample> examples = new ArrayList<>();
        int position = 1;
        for (LessonExampleDto example : request.examples() == null ? List.<LessonExampleDto>of() : request.examples()) {
            examples.add(new LessonExample(example.imageUrl().trim(), example.text().trim(), position++));
        }
        lesson.replaceExamples(examples);
    }
}
