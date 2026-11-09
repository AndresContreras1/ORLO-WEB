package com.orlo.api.service;

import com.orlo.api.dto.QuizDto;
import com.orlo.api.dto.QuizScoreRequest;
import com.orlo.api.dto.QuizScoreResponse;
import com.orlo.api.dto.admin.AdminQuestionDto;
import com.orlo.api.dto.admin.QuestionRequest;

import java.util.List;

public interface QuizService {

    QuizDto activeQuiz();

    /** Suma los puntos de las opciones elegidas: el estilo con más puntos es el principal y el segundo, el secundario. */
    QuizScoreResponse score(String uid, boolean premium, QuizScoreRequest request);

    List<AdminQuestionDto> findAllQuestions();

    AdminQuestionDto findQuestion(Long id);

    AdminQuestionDto createQuestion(QuestionRequest request);

    AdminQuestionDto updateQuestion(Long id, QuestionRequest request);

    void deleteQuestion(Long id);
}
