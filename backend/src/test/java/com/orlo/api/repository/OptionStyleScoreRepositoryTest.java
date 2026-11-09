package com.orlo.api.repository;

import com.orlo.api.entities.OptionStyleScore;
import com.orlo.api.entities.QuizOption;
import com.orlo.api.entities.QuizQuestion;
import com.orlo.api.entities.Style;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OptionStyleScoreRepositoryTest {

    @Autowired
    private StyleRepository styleRepository;
    @Autowired
    private QuizQuestionRepository questionRepository;
    @Autowired
    private OptionStyleScoreRepository scoreRepository;

    private Style casual;
    private Style urbano;

    @BeforeEach
    void setUp() {
        casual = styleRepository.save(new Style("casual", "Casual", "desc", List.of("#000000"), List.of()));
        urbano = styleRepository.save(new Style("urbano", "Urbano", "desc", List.of("#FFFFFF"), List.of()));
    }

    @Test
    void findForOptions_activeQuestion_returnsScoresWithStyle() {
        // Arrange
        QuizOption option = saveQuestion(true);

        // Act
        List<OptionStyleScore> scores = scoreRepository.findForOptions(List.of(option.getId()));

        // Assert
        assertThat(scores).hasSize(2);
        assertThat(scores).extracting(s -> s.getStyle().getCode()).containsExactlyInAnyOrder("casual", "urbano");
    }

    @Test
    void findForOptions_inactiveQuestion_isIgnored() {
        QuizOption option = saveQuestion(false);

        List<OptionStyleScore> scores = scoreRepository.findForOptions(List.of(option.getId()));

        assertThat(scores).isEmpty();
    }

    @Test
    void existsByStyleId_styleInPointsTable_true() {
        saveQuestion(true);

        assertThat(scoreRepository.existsByStyleId(casual.getId())).isTrue();
    }

    private QuizOption saveQuestion(boolean active) {
        QuizQuestion question = new QuizQuestion("¿Qué usas?", null, false, 1);
        question.setActive(active);
        QuizOption option = new QuizOption("Jeans", null, 1);
        option.addScore(casual, 3);
        option.addScore(urbano, 1);
        question.addOption(option);
        questionRepository.saveAndFlush(question);
        return question.getOptions().get(0);
    }
}
