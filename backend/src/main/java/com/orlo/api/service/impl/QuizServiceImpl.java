package com.orlo.api.service.impl;

import com.orlo.api.dto.QuizAnswerRequest;
import com.orlo.api.dto.QuizDto;
import com.orlo.api.dto.QuizQuestionDto;
import com.orlo.api.dto.QuizScoreRequest;
import com.orlo.api.dto.QuizScoreResponse;
import com.orlo.api.dto.StyleDto;
import com.orlo.api.dto.admin.AdminQuestionDto;
import com.orlo.api.dto.admin.OptionRequest;
import com.orlo.api.dto.admin.QuestionRequest;
import com.orlo.api.entities.OptionStyleScore;
import com.orlo.api.entities.QuizOption;
import com.orlo.api.entities.QuizQuestion;
import com.orlo.api.entities.QuizResult;
import com.orlo.api.entities.Style;
import com.orlo.api.exception.BusinessRuleException;
import com.orlo.api.exception.NotFoundException;
import com.orlo.api.repository.OptionStyleScoreRepository;
import com.orlo.api.repository.QuizQuestionRepository;
import com.orlo.api.repository.QuizResultRepository;
import com.orlo.api.repository.StyleRepository;
import com.orlo.api.service.QuizService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuizServiceImpl implements QuizService {

    private final QuizQuestionRepository questionRepository;
    private final OptionStyleScoreRepository scoreRepository;
    private final StyleRepository styleRepository;
    private final QuizResultRepository resultRepository;
    private final int freeGallerySize;

    public QuizServiceImpl(QuizQuestionRepository questionRepository, OptionStyleScoreRepository scoreRepository,
                           StyleRepository styleRepository, QuizResultRepository resultRepository,
                           @Value("${app.premium.free-gallery-size}") int freeGallerySize) {
        this.questionRepository = questionRepository;
        this.scoreRepository = scoreRepository;
        this.styleRepository = styleRepository;
        this.resultRepository = resultRepository;
        this.freeGallerySize = freeGallerySize;
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDto activeQuiz() {
        Instant lastUpdate = questionRepository.findLastUpdate();
        List<QuizQuestionDto> questions = questionRepository.findByActiveTrueOrderByPositionAsc().stream()
                .map(QuizQuestionDto::from).toList();
        return new QuizDto(lastUpdate == null ? 0L : lastUpdate.getEpochSecond(), questions);
    }

    @Override
    @Transactional
    public QuizScoreResponse score(String uid, boolean premium, QuizScoreRequest request) {
        Set<Long> chosenOptions = validateAnswers(request.answers());

        List<Style> styles = styleRepository.findAll();
        if (styles.size() < 2) {
            throw new BusinessRuleException("Se necesitan al menos dos estilos para calcular el resultado");
        }
        Map<Long, Integer> totals = new HashMap<>();
        styles.forEach(s -> totals.put(s.getId(), 0));
        for (OptionStyleScore score : scoreRepository.findForOptions(chosenOptions)) {
            totals.merge(score.getStyle().getId(), score.getPoints(), Integer::sum);
        }

        List<Style> ranking = styles.stream()
                .sorted(Comparator.comparing((Style s) -> totals.get(s.getId())).reversed()
                        .thenComparing(Style::getName))
                .toList();
        Style primary = ranking.get(0);
        Style secondary = ranking.get(1);
        resultRepository.save(new QuizResult(uid, primary.getCode(), secondary.getCode()));

        Map<String, Integer> scoresByCode = new LinkedHashMap<>();
        ranking.forEach(s -> scoresByCode.put(s.getCode(), totals.get(s.getId())));
        int limit = premium ? Integer.MAX_VALUE : freeGallerySize;
        return new QuizScoreResponse(StyleDto.from(primary, limit), StyleDto.from(secondary, limit), scoresByCode);
    }

    /** Cada respuesta debe ser de una pregunta activa, con opciones de esa misma pregunta. */
    private Set<Long> validateAnswers(List<QuizAnswerRequest> answers) {
        Map<Long, QuizQuestion> active = questionRepository.findByActiveTrueOrderByPositionAsc().stream()
                .collect(Collectors.toMap(QuizQuestion::getId, Function.identity()));
        Set<Long> answered = new HashSet<>();
        Set<Long> chosen = new HashSet<>();
        for (QuizAnswerRequest answer : answers) {
            QuizQuestion question = active.get(answer.questionId());
            if (question == null) {
                throw new BusinessRuleException("La pregunta " + answer.questionId() + " no está en el cuestionario");
            }
            if (!answered.add(question.getId())) {
                throw new BusinessRuleException("La pregunta " + question.getId() + " está respondida dos veces");
            }
            if (!question.isMultiple() && answer.optionIds().size() > 1) {
                throw new BusinessRuleException("La pregunta " + question.getId() + " admite una sola opción");
            }
            Set<Long> validOptions = question.getOptions().stream().map(QuizOption::getId).collect(Collectors.toSet());
            for (Long optionId : answer.optionIds()) {
                if (!validOptions.contains(optionId)) {
                    throw new BusinessRuleException("La opción " + optionId + " no pertenece a la pregunta " + question.getId());
                }
                chosen.add(optionId);
            }
        }
        return chosen;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminQuestionDto> findAllQuestions() {
        return questionRepository.findAllByOrderByPositionAsc().stream().map(AdminQuestionDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminQuestionDto findQuestion(Long id) {
        return AdminQuestionDto.from(load(id));
    }

    @Override
    @Transactional
    public AdminQuestionDto createQuestion(QuestionRequest request) {
        QuizQuestion question = new QuizQuestion();
        apply(question, request);
        return AdminQuestionDto.from(questionRepository.save(question));
    }

    @Override
    @Transactional
    public AdminQuestionDto updateQuestion(Long id, QuestionRequest request) {
        QuizQuestion question = load(id);
        apply(question, request);
        questionRepository.flush();
        return AdminQuestionDto.from(question);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        questionRepository.delete(load(id));
    }

    private QuizQuestion load(Long id) {
        return questionRepository.findById(id).orElseThrow(() -> new NotFoundException("Pregunta", id));
    }

    /** Reemplaza las opciones y su fila de la tabla de puntos. Los puntos en 0 no se guardan. */
    private void apply(QuizQuestion question, QuestionRequest request) {
        Map<Long, Style> styles = styleRepository.findAll().stream()
                .collect(Collectors.toMap(Style::getId, Function.identity()));
        question.setText(request.text().trim());
        question.setHelpText(request.helpText() == null || request.helpText().isBlank() ? null : request.helpText().trim());
        question.setMultiple(request.multiple());
        question.setPosition(request.order());
        question.setActive(request.active());
        question.getOptions().clear();
        int position = 1;
        for (OptionRequest optionRequest : request.options()) {
            QuizOption option = new QuizOption(optionRequest.text().trim(),
                    optionRequest.imageUrl() == null || optionRequest.imageUrl().isBlank() ? null : optionRequest.imageUrl().trim(),
                    position++);
            optionRequest.scores().forEach((styleId, points) -> {
                Style style = styles.get(styleId);
                if (style == null) {
                    throw new BusinessRuleException("El estilo " + styleId + " no existe");
                }
                if (points > 0) {
                    option.addScore(style, points);
                }
            });
            question.addOption(option);
        }
        question.setUpdatedAt(Instant.now());
    }
}
