package com.orlo.api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Registro de un cuestionario completado. Alimenta las métricas del panel. */
@Entity
@Table(name = "quiz_results")
@Getter
@Setter
@NoArgsConstructor
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String uid;

    @Column(name = "primary_code", nullable = false, length = 40)
    private String primaryCode;

    @Column(name = "secondary_code", nullable = false, length = 40)
    private String secondaryCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public QuizResult(String uid, String primaryCode, String secondaryCode) {
        this.uid = uid;
        this.primaryCode = primaryCode;
        this.secondaryCode = secondaryCode;
        this.createdAt = Instant.now();
    }
}
