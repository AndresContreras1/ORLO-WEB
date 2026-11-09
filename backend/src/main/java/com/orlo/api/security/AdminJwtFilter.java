package com.orlo.api.security;

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

/**
 * Lee el JWT del panel en las rutas /api/v1/admin/**. No rechaza: si el token es válido llena el
 * contexto de seguridad y la autorización la decide la cadena de filtros.
 */
@Component
public class AdminJwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public AdminJwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/admin/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        BearerTokens.from(request)
                .flatMap(jwtService::parse)
                .ifPresent(token -> {
                    var authorities = token.roles().stream().map(SimpleGrantedAuthority::new).toList();
                    var authentication = new UsernamePasswordAuthenticationToken(token.email(), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
        chain.doFilter(request, response);
    }
}
