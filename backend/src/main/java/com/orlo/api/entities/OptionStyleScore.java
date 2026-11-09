package com.orlo.api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla de puntos del cuestionario: cuántos puntos da una opción a un estilo.
 * Es una tabla intermedia con atributo propio, por eso es entidad.
 */
@Entity
@Table(name = "option_style_scores",
        uniqueConstraints = @UniqueConstraint(columnNames = {"option_id", "style_id"}))
@Getter
@Setter
@NoArgsConstructor
public class OptionStyleScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private QuizOption option;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "style_id", nullable = false)
    private Style style;

    @Column(nullable = false)
    private int points;

    public OptionStyleScore(QuizOption option, Style style, int points) {
        this.option = option;
        this.style = style;
        this.points = points;
    }
}
