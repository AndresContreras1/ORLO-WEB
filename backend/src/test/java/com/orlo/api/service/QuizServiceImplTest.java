package com.orlo.api.service;

import com.orlo.api.dto.QuizAnswerRequest;
import com.orlo.api.dto.QuizScoreRequest;
import com.orlo.api.dto.QuizScoreResponse;
import com.orlo.api.entities.OptionStyleScore;
import com.orlo.api.entities.QuizOption;
import com.orlo.api.entities.QuizQuestion;
import com.orlo.api.entities.QuizResult;
import com.orlo.api.entities.Style;
import com.orlo.api.exception.BusinessRuleException;
import com.orlo.api.repository.OptionStyleScoreRepository;
import com.orlo.api.repository.QuizQuestionRepository;
import com.orlo.api.repository.QuizResultRepository;
import com.orlo.api.repository.StyleRepository;
import com.orlo.api.service.impl.QuizServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock
    private QuizQuestionRepository questionRepository;
    @Mock
    private OptionStyleScoreRepository scoreRepository;
    @Mock
    private StyleRepository styleRepository;
    @Mock
    private QuizResultRepository resultRepository;

    private QuizServiceImpl quizService;
    private Style casual;
    private Style urbano;
    private Style clasico;
    private QuizQuestion question;

    @BeforeEach
    void setUp() {
        quizService = new QuizServiceImpl(questionRepository, scoreRepository, styleRepository, resultRepository, 2);
        casual = style(1L, "casual", "Casual");
        urbano = style(2L, "urbano", "Urbano");
        clasico = style(3L, "clasico", "Clásico");
        question = new QuizQuestion("¿Qué usas?", null, false, 1);
        question.setId(10L);
        question.addOption(option(100L, "Jeans"));
        question.addOption(option(101L, "Camisa"));
    }

    @Test
    void score_validAnswers_primaryAndSecondaryByPoints() {
        // Arrange
        when(questionRepository.findByActiveTrueOrderByPositionAsc()).thenReturn(List.of(question));
        when(styleRepository.findAll()).thenReturn(List.of(casual, urbano, clasico));
        when(scoreRepository.findForOptions(any())).thenReturn(List.of(
                score(urbano, 3), score(casual, 1), score(clasico, 2)));
        var request = new QuizScoreRequest(List.of(new QuizAnswerRequest(10L, List.of(100L))));

        // Act
        QuizScoreResponse response = quizService.score("uid-1", false, request);

        // Assert
        assertThat(response.primary().code()).isEqualTo("urbano");
        assertThat(response.secondary().code()).isEqualTo("clasico");
        assertThat(response.scores()).containsEntry("urbano", 3).containsEntry("casual", 1);
        assertThat(response.primary().gallery()).hasSize(2);
        ArgumentCaptor<QuizResult> saved = ArgumentCaptor.forClass(QuizResult.class);
        verify(resultRepository).save(saved.capture());
        assertThat(saved.getValue().getPrimaryCode()).isEqualTo("urbano");
    }

    @Test
    void score_tie_breaksByName() {
        when(questionRepository.findByActiveTrueOrderByPositionAsc()).thenReturn(List.of(question));
        when(styleRepository.findAll()).thenReturn(List.of(urbano, casual, clasico));
        when(scoreRepository.findForOptions(any())).thenReturn(List.of());
        var request = new QuizScoreRequest(List.of(new QuizAnswerRequest(10L, List.of(100L))));

        QuizScoreResponse response = quizService.score("uid-1", true, request);

        assertThat(response.primary().code()).isEqualTo("casual");
        assertThat(response.secondary().code()).isEqualTo("clasico");
        assertThat(response.primary().gallery()).hasSize(4);
    }

    @Test
    void score_optionFromAnotherQuestion_throwsBusinessRule() {
        when(questionRepository.findByActiveTrueOrderByPositionAsc()).thenReturn(List.of(question));
        var request = new QuizScoreRequest(List.of(new QuizAnswerRequest(10L, List.of(999L))));

        assertThatThrownBy(() -> quizService.score("uid-1", false, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("999");
        verify(resultRepository, never()).save(any());
    }

    @Test
    void score_twoOptionsInSingleQuestion_throwsBusinessRule() {
        when(questionRepository.findByActiveTrueOrderByPositionAsc()).thenReturn(List.of(question));
        var request = new QuizScoreRequest(List.of(new QuizAnswerRequest(10L, List.of(100L, 101L))));

        assertThatThrownBy(() -> quizService.score("uid-1", false, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("una sola opción");
    }

    @Test
    void score_inactiveQuestion_throwsBusinessRule() {
        when(questionRepository.findByActiveTrueOrderByPositionAsc()).thenReturn(List.of());
        var request = new QuizScoreRequest(List.of(new QuizAnswerRequest(10L, List.of(100L))));

        assertThatThrownBy(() -> quizService.score("uid-1", false, request))
                .isInstanceOf(BusinessRuleException.class);
    }

    private static Style style(Long id, String code, String name) {
        Style style = new Style(code, name, "desc", List.of("#000000"),
                List.of("a.jpg", "b.jpg", "c.jpg", "d.jpg"));
        style.setId(id);
        return style;
    }

    private static QuizOption option(Long id, String text) {
        QuizOption option = new QuizOption(text, null, 1);
        option.setId(id);
        return option;
    }

    private static OptionStyleScore score(Style style, int points) {
        return new OptionStyleScore(null, style, points);
    }
}
