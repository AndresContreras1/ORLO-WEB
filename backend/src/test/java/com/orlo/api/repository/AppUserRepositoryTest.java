package com.orlo.api.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
// Usa la H2 del perfil test (modo PostgreSQL) y no la embebida por defecto: así entiende ON CONFLICT
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void insertIfMissing_twice_keepsOneUser() {
        Instant now = Instant.parse("2026-11-10T15:00:00Z");

        appUserRepository.insertIfMissing("uid-1", "a@b.co", now);
        appUserRepository.insertIfMissing("uid-1", "otro@b.co", now.plusSeconds(5));

        assertThat(appUserRepository.count()).isEqualTo(1);
        assertThat(appUserRepository.findById("uid-1")).hasValueSatisfying(u -> {
            assertThat(u.getEmail()).isEqualTo("a@b.co");
            assertThat(u.isPremium()).isFalse();
        });
    }
}
