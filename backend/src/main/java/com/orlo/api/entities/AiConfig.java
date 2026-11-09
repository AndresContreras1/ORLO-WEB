package com.orlo.api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Configuración del asesor IA editable desde el panel. Hay una sola fila (id = 1). */
@Entity
@Table(name = "ai_config")
@Getter
@Setter
@NoArgsConstructor
public class AiConfig {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "system_prompt", nullable = false, length = 8000)
    private String systemPrompt;

    @Column(nullable = false, length = 60)
    private String model;

    @Column(name = "daily_limit", nullable = false)
    private int dailyLimit;

    @Column(name = "max_output_tokens", nullable = false)
    private int maxOutputTokens;
}
