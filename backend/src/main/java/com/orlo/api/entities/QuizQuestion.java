package com.orlo.api.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Pregunta del cuestionario de estilo. Se responde tocando una o varias opciones. */
@Entity
@Table(name = "quiz_questions")
@Getter
@Setter
@NoArgsConstructor
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String text;

    @Column(name = "help_text", length = 300)
    private String helpText;

    @Column(nullable = false)
    private boolean multiple;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<QuizOption> options = new ArrayList<>();

    public QuizQuestion(String text, String helpText, boolean multiple, int position) {
        this.text = text;
        this.helpText = helpText;
        this.multiple = multiple;
        this.position = position;
    }

    public void addOption(QuizOption option) {
        option.setQuestion(this);
        options.add(option);
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }
}
