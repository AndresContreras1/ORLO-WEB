package com.orlo.api.service.impl;

import com.orlo.api.entities.AiUsage;
import com.orlo.api.exception.LimitReachedException;
import com.orlo.api.repository.AiUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** En el paquete de la implementación para usar el reloj fijo del constructor de pruebas. */
@ExtendWith(MockitoExtension.class)
class UsageServiceImplTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 9);

    @Mock
    private AiUsageRepository aiUsageRepository;

    private UsageServiceImpl usageService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-09T15:00:00Z"), UsageServiceImpl.ZONE);
        usageService = new UsageServiceImpl(aiUsageRepository, clock);
    }

    @Test
    void reserve_belowLimit_incrementsAndReturnsRemaining() {
        AiUsage usage = usage(4);
        when(aiUsageRepository.findForUpdate("uid-1", DAY)).thenReturn(Optional.of(usage));

        int remaining = usageService.reserve("uid-1", 5);

        assertThat(remaining).isZero();
        assertThat(usage.getMessages()).isEqualTo(5);
    }

    @Test
    void reserve_atLimit_throwsLimitReached() {
        when(aiUsageRepository.findForUpdate("uid-1", DAY)).thenReturn(Optional.of(usage(5)));

        assertThatThrownBy(() -> usageService.reserve("uid-1", 5))
                .isInstanceOf(LimitReachedException.class)
                .hasFieldOrPropertyWithValue("code", "LIMIT_REACHED");
    }

    @Test
    void release_afterReserve_givesMessageBack() {
        AiUsage usage = usage(3);
        when(aiUsageRepository.findForUpdate("uid-1", DAY)).thenReturn(Optional.of(usage));

        usageService.release("uid-1");

        assertThat(usage.getMessages()).isEqualTo(2);
    }

    @Test
    void cost_haikuPrices_perMillionTokens() {
        assertThat(UsageServiceImpl.cost("claude-haiku-5-5", 1_000_000, 1_000_000)).isEqualByComparingTo("0.60");
        assertThat(UsageServiceImpl.cost("claude-sonnet-5-5", 1000, 0)).isEqualByComparingTo("0.002");
    }

    private static AiUsage usage(int messages) {
        AiUsage usage = new AiUsage("uid-1", DAY);
        usage.setMessages(messages);
        return usage;
    }
}
