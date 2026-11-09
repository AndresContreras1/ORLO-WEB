package com.orlo.api.service;

import com.orlo.api.dto.LessonDetailDto;
import com.orlo.api.dto.LessonSummaryDto;
import com.orlo.api.dto.admin.AdminLessonDto;
import com.orlo.api.dto.admin.LessonRequest;

import java.util.List;

public interface LessonService {

    List<LessonSummaryDto> listPublished();

    /** Lección completa. Si no es la muestra gratis, exige premium. */
    LessonDetailDto detail(Long id, boolean premium);

    List<AdminLessonDto> findAll();

    AdminLessonDto findById(Long id);

    AdminLessonDto create(LessonRequest request);

    AdminLessonDto update(Long id, LessonRequest request);

    void delete(Long id);
}
