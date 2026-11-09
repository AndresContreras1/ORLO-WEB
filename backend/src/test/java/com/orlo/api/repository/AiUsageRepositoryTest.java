package com.orlo.api.repository;

import com.orlo.api.entities.AiUsage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AiUsageRepositoryTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 9);

    @Autowired
    private AiUsageRepository aiUsageRepository;

    @Test
    void sums_twoUsers_addMessagesAndCost() {
        save("uid-1", DAY, 3, "0.001000");
        save("uid-2", DAY, 2, "0.000500");
        save("uid-1", DAY.minusDays(1), 4, "0.002000");

        assertThat(aiUsageRepository.sumMessages()).isEqualTo(9);
        assertThat(aiUsageRepository.sumMessagesOn(DAY)).isEqualTo(5);
        assertThat(aiUsageRepository.sumCost()).isEqualByComparingTo("0.0035");
    }

    @Test
    void sums_noRows_returnZero() {
        assertThat(aiUsageRepository.sumMessages()).isZero();
        assertThat(aiUsageRepository.sumCost()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void findByUidAndUsageDate_otherDay_empty() {
        save("uid-1", DAY, 1, "0");

        assertThat(aiUsageRepository.findByUidAndUsageDate("uid-1", DAY.plusDays(1))).isEmpty();
    }

    private void save(String uid, LocalDate day, int messages, String cost) {
        AiUsage usage = new AiUsage(uid, day);
        usage.setMessages(messages);
        usage.setCostUsd(new BigDecimal(cost));
        aiUsageRepository.save(usage);
    }
}
