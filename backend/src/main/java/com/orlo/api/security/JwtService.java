package com.orlo.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/** Genera y valida los JWT del panel de administración. El secreto llega por variable de entorno. */
@Service
public class JwtService {

    private static final String ROLES_CLAIM = "roles";

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    public String generate(String email, List<String> roles) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim(ROLES_CLAIM, roles)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /** Devuelve los datos del token o vacío si la firma no coincide o ya venció. */
    public Optional<AdminToken> parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            List<?> rawRoles = claims.get(ROLES_CLAIM, List.class);
            List<String> roles = rawRoles == null ? List.of() : rawRoles.stream().map(String::valueOf).toList();
            return Optional.of(new AdminToken(claims.getSubject(), roles));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long expirationSeconds() {
        return expirationMs / 1000;
    }

    public record AdminToken(String email, List<String> roles) {
    }
}
