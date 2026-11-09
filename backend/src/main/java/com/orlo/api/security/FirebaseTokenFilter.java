package com.orlo.api.security;

import com.orlo.api.client.FirebaseAuthClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Valida el ID token de Firebase que envía la app en /api/v1/** (menos el panel).
 * Igual que el filtro del panel, no rechaza: solo identifica.
 */
@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private final FirebaseAuthClient firebaseAuthClient;

    public FirebaseTokenFilter(FirebaseAuthClient firebaseAuthClient) {
        this.firebaseAuthClient = firebaseAuthClient;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/api/v1/") || uri.startsWith("/api/v1/admin/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        BearerTokens.from(request)
                .flatMap(firebaseAuthClient::verify)
                .ifPresent(identity -> {
                    var principal = new AppPrincipal(identity.uid(), identity.email());
                    var authentication = new UsernamePasswordAuthenticationToken(principal, null,
                            List.of(new SimpleGrantedAuthority(AppPrincipal.AUTHORITY)));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
        chain.doFilter(request, response);
    }
}
