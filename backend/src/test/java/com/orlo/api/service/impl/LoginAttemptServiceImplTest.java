package com.orlo.api.service.impl;

import com.orlo.api.exception.TooManyAttemptsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** En el paquete de la implementación para usar el reloj de prueba del constructor. */
class LoginAttemptServiceImplTest {

    private static final String EMAIL = "admin@orlo.co";
    private static final String IP = "203.0.113.7";

    private MutableClock clock;
    private LoginAttemptServiceImpl service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-11-10T15:00:00Z"));
        service = new LoginAttemptServiceImpl(10, Duration.ofMinutes(5), 5, Duration.ofMinutes(15), clock);
    }

    @Test
    void fiveFailures_lockTheAccountWithRetryAfter() {
        for (int i = 0; i < 5; i++) {
            service.checkAllowed(EMAIL, "198.51.100." + i);
            service.recordFailure(EMAIL, "198.51.100." + i);
        }

        // Desde otra IP también: el bloqueo es de la cuenta, aunque se cambie X-Forwarded-For
        assertThatThrownBy(() -> service.checkAllowed(EMAIL, "192.0.2.1"))
                .isInstanceOf(TooManyAttemptsException.class)
                .hasFieldOrPropertyWithValue("code", "TOO_MANY_REQUESTS")
                .hasFieldOrPropertyWithValue("retryAfterSeconds", 900L);
    }

    @Test
    void lock_isTemporary() {
        for (int i = 0; i < 5; i++) {
            service.recordFailure(EMAIL, IP);
        }
        clock.advance(Duration.ofMinutes(15));

        assertThatCode(() -> service.checkAllowed(EMAIL, IP)).doesNotThrowAnyException();
    }

    @Test
    void success_resetsTheFailures() {
        for (int i = 0; i < 4; i++) {
            service.recordFailure(EMAIL, IP);
        }
        service.recordSuccess(EMAIL);
        service.recordFailure(EMAIL, IP);

        assertThatCode(() -> service.checkAllowed(EMAIL, IP)).doesNotThrowAnyException();
    }

    @Test
    void ip_isLimitedPerWindow() {
        for (int i = 0; i < 10; i++) {
            service.checkAllowed("cuenta" + i + "@orlo.co", IP);
        }

        assertThatThrownBy(() -> service.checkAllowed("otra@orlo.co", IP)).isInstanceOf(TooManyAttemptsException.class);

        clock.advance(Duration.ofMinutes(5));
        assertThatCode(() -> service.checkAllowed("otra@orlo.co", IP)).doesNotThrowAnyException();
    }

    @Test
    void failuresOfOneAccount_doNotLockAnother() {
        for (int i = 0; i < 5; i++) {
            service.recordFailure(EMAIL, IP);
        }

        assertThatCode(() -> service.checkAllowed("editor@orlo.co", "192.0.2.9")).doesNotThrowAnyException();
        assertThat(clock.instant()).isEqualTo(Instant.parse("2026-11-10T15:00:00Z"));
    }

    /** Reloj que avanza solo cuando la prueba lo pide. */
    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
