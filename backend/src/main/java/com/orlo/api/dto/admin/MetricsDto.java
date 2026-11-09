package com.orlo.api.dto.admin;

import java.math.BigDecimal;

/** Métricas básicas para ajustar precio y límites de la IA. */
public record MetricsDto(long totalUsers, long premiumUsers, double conversionRate, long quizzesCompleted,
                         long advisorMessages, long advisorMessagesToday, BigDecimal aiCostUsd,
                         long publishedLessons, long activeStores) {
}
