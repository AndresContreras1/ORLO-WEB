package com.orlo.api.service.impl;

import com.orlo.api.exception.TooManyAttemptsException;
import com.orlo.api.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contadores en memoria: la API corre en una sola instancia. Con varias instancias habría que
 * llevarlos a un almacén compartido (Redis), porque cada una contaría por su lado.
 * <ul>
 *   <li>Por IP: máximo {@code app.login.max-attempts-per-ip} intentos (buenos o malos) por ventana.</li>
 *   <li>Por cuenta: tras {@code app.login.max-failures} fallos seguidos, bloqueo de {@code app.login.lock-duration}.</li>
 * </ul>
 */
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    /** Por encima de este tamaño se limpian las entradas vencidas, para que la memoria no crezca sin fin. */
    private static final int PRUNE_THRESHOLD = 10_000;

    private final int maxAttemptsPerIp;
    private final Duration ipWindow;
    private final int maxFailures;
    private final Duration lockDuration;
    private final Clock clock;

    private final Map<String, Window> attemptsByIp = new ConcurrentHashMap<>();
    private final Map<String, Failures> failuresByEmail = new ConcurrentHashMap<>();

    @Autowired
    public LoginAttemptServiceImpl(@Value("${app.login.max-attempts-per-ip:10}") int maxAttemptsPerIp,
                                   @Value("${app.login.ip-window:PT5M}") Duration ipWindow,
                                   @Value("${app.login.max-failures:5}") int maxFailures,
                                   @Value("${app.login.lock-duration:PT15M}") Duration lockDuration) {
        this(maxAttemptsPerIp, ipWindow, maxFailures, lockDuration, Clock.systemUTC());
    }

    LoginAttemptServiceImpl(int maxAttemptsPerIp, Duration ipWindow, int maxFailures, Duration lockDuration, Clock clock) {
        this.maxAttemptsPerIp = maxAttemptsPerIp;
        this.ipWindow = ipWindow;
        this.maxFailures = maxFailures;
        this.lockDuration = lockDuration;
        this.clock = clock;
    }

    @Override
    public void checkAllowed(String email, String ip) {
        Instant now = clock.instant();
        Failures failures = failuresByEmail.get(email);
        if (failures != null && failures.lockedUntil() != null && now.isBefore(failures.lockedUntil())) {
            throw new TooManyAttemptsException(Duration.between(now, failures.lockedUntil()).toSeconds());
        }
        Window window = attemptsByIp.compute(ip, (k, w) ->
                w == null || !now.isBefore(w.start().plus(ipWindow)) ? new Window(now, 1) : new Window(w.start(), w.count() + 1));
        if (window.count() > maxAttemptsPerIp) {
            throw new TooManyAttemptsException(Duration.between(now, window.start().plus(ipWindow)).toSeconds());
        }
        prune(now);
    }

    @Override
    public void recordFailure(String email, String ip) {
        Instant now = clock.instant();
        failuresByEmail.compute(email, (k, f) -> {
            int count = (f == null || f.lockedUntil() != null && !now.isBefore(f.lockedUntil())) ? 1 : f.count() + 1;
            return new Failures(count, count >= maxFailures ? now.plus(lockDuration) : null);
        });
    }

    @Override
    public void recordSuccess(String email) {
        failuresByEmail.remove(email);
    }

    private void prune(Instant now) {
        if (attemptsByIp.size() > PRUNE_THRESHOLD) {
            attemptsByIp.values().removeIf(w -> !now.isBefore(w.start().plus(ipWindow)));
        }
        if (failuresByEmail.size() > PRUNE_THRESHOLD) {
            failuresByEmail.values().removeIf(f -> f.lockedUntil() == null || !now.isBefore(f.lockedUntil()));
        }
    }

    private record Window(Instant start, int count) {
    }

    /** lockedUntil es null mientras la cuenta no ha llegado al máximo de fallos. */
    private record Failures(int count, Instant lockedUntil) {
    }
}
