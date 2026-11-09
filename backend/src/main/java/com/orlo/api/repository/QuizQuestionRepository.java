package com.orlo.api.repository;

import com.orlo.api.entities.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByActiveTrueOrderByPositionAsc();

    List<QuizQuestion> findAllByOrderByPositionAsc();

    @Query("SELECT MAX(q.updatedAt) FROM QuizQuestion q")
    Instant findLastUpdate();
}
