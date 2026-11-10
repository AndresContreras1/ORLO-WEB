package com.orlo.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Límite de peticiones por usuario autenticado (uid de Firebase o correo del panel): por defecto
 * 120 por minuto, de sobra para usar la web o la app. Frena los scripts que repiten peticiones
 * con un token válido. Va después de los filtros de token; las peticiones sin identidad ya
 * terminan en 401. Contadores en memoria: la API corre en una sola instancia.
 */
@Component
public class ApiRateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000;
    private static final int PRUNE_THRESHOLD = 10_000;

    private final int maxPerMinute;
    private final ProblemDetailWriter problemDetailWriter;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Autowired
    public ApiRateLimitFilter(@Value("${app.rate-limit.requests-per-minute:120}") int maxPerMinute,
                              ProblemDetailWriter problemDetailWriter) {
        this(maxPerMinute, problemDetailWriter, Clock.systemUTC());
    }

    ApiRateLimitFilter(int maxPerMinute, ProblemDetailWriter problemDetailWriter, Clock clock) {
        this.maxPerMinute = maxPerMinute;
        this.problemDetailWriter = problemDetailWriter;
        this.clock = clock;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String key = auth.getPrincipal() instanceof AppPrincipal app ? "app:" + app.uid() : "panel:" + auth.getName();
            long now = clock.millis();
            Window window = windows.compute(key, (k, w) ->
                    w == null || now - w.start() >= WINDOW_MS ? new Window(now, 1) : new Window(w.start(), w.count() + 1));
            if (window.count() > maxPerMinute) {
                long retryAfter = Math.max(1, (window.start() + WINDOW_MS - now + 999) / 1000);
                response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter));
                problemDetailWriter.write(response, HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS",
                        "Hiciste muchas peticiones seguidas. Espera un momento y vuelve a intentar");
                return;
            }
            if (windows.size() > PRUNE_THRESHOLD) {
                windows.values().removeIf(w -> now - w.start() >= WINDOW_MS);
            }
        }
        chain.doFilter(request, response);
    }

    private record Window(long start, int count) {
    }
}
