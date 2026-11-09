package com.orlo.api.repository;

import com.orlo.api.entities.OptionStyleScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OptionStyleScoreRepository extends JpaRepository<OptionStyleScore, Long> {

    /** Puntos de las opciones elegidas, solo de preguntas activas. */
    @Query("""
            SELECT s FROM OptionStyleScore s
            JOIN FETCH s.style
            WHERE s.option.id IN :optionIds AND s.option.question.active = true
            """)
    List<OptionStyleScore> findForOptions(@Param("optionIds") Collection<Long> optionIds);

    boolean existsByStyleId(Long styleId);
}
