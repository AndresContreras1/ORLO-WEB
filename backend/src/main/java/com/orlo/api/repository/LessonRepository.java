package com.orlo.api.repository;

import com.orlo.api.entities.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByPublishedTrueOrderByPositionAsc();

    List<Lesson> findAllByOrderByPositionAsc();

    Optional<Lesson> findByIdAndPublishedTrue(Long id);
}
