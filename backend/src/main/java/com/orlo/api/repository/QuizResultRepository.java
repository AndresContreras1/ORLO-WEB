package com.orlo.api.repository;

import com.orlo.api.entities.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    void deleteByUid(String uid);
}
