package com.orlo.api.repository;

import com.orlo.api.entities.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface AppUserRepository extends JpaRepository<AppUser, String> {

    long countByPremiumTrue();

    Page<AppUser> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    /**
     * Crea el usuario si todavía no existe. Si dos peticiones del mismo usuario nuevo llegan a la
     * vez, la segunda no choca con la llave primaria: ON CONFLICT la convierte en "no hacer nada".
     */
    @Modifying
    @Query(value = """
            INSERT INTO app_users (uid, email, premium, created_at, last_seen_at)
            VALUES (:uid, :email, FALSE, :now, :now)
            ON CONFLICT DO NOTHING""", nativeQuery = true)
    void insertIfMissing(@Param("uid") String uid, @Param("email") String email, @Param("now") Instant now);
}
