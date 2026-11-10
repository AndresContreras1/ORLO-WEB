package com.orlo.api.repository;

import com.orlo.api.entities.AiUsage;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface AiUsageRepository extends JpaRepository<AiUsage, Long> {

    Optional<AiUsage> findByUidAndUsageDate(String uid, LocalDate usageDate);

    /** Bloquea la fila del día para que dos mensajes simultáneos no pasen el límite. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM AiUsage u WHERE u.uid = :uid AND u.usageDate = :day")
    Optional<AiUsage> findForUpdate(@Param("uid") String uid, @Param("day") LocalDate day);

    /**
     * Crea la fila del día en cero si no existe, sin fallar cuando otra petición la creó al mismo
     * tiempo (uk_ai_usage_uid_date). Después se bloquea con findForUpdate.
     */
    @Modifying
    @Query(value = """
            INSERT INTO ai_usage (uid, usage_date, messages, daily_outfits, input_tokens, output_tokens, cost_usd)
            VALUES (:uid, :day, 0, 0, 0, 0, 0)
            ON CONFLICT DO NOTHING""", nativeQuery = true)
    void insertIfMissing(@Param("uid") String uid, @Param("day") LocalDate day);

    @Query("SELECT COALESCE(SUM(u.messages), 0) FROM AiUsage u")
    long sumMessages();

    @Query("SELECT COALESCE(SUM(u.messages), 0) FROM AiUsage u WHERE u.usageDate = :day")
    long sumMessagesOn(@Param("day") LocalDate day);

    @Query("SELECT COALESCE(SUM(u.costUsd), 0) FROM AiUsage u")
    BigDecimal sumCost();

    void deleteByUid(String uid);
}
