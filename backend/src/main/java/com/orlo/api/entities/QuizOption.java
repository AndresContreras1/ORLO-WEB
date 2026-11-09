package com.orlo.api.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Opción de una pregunta. Cada opción suma puntos a uno o varios estilos. */
@Entity
@Table(name = "quiz_options")
@Getter
@Setter
@NoArgsConstructor
public class QuizOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(nullable = false, length = 200)
    private String text;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private int position;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionStyleScore> scores = new ArrayList<>();

    public QuizOption(String text, String imageUrl, int position) {
        this.text = text;
        this.imageUrl = imageUrl;
        this.position = position;
    }

    public void addScore(Style style, int points) {
        scores.add(new OptionStyleScore(this, style, points));
    }
}
