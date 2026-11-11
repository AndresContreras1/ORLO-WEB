package com.orlo.api.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Tokens firmados con la llave correcta pero armados a mano: la firma sola no basta para entrar. */
class JwtServiceTest {

    private static final String SECRET = "dGVzdC1zZWNyZXQtc29sby1wYXJhLXBydWViYXMtb3Jsby1hcGktMjAyNi1zZWd1cmlkYWQ=";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

    private final JwtService jwtService = new JwtService(SECRET, 1_800_000);

    @Test
    void parse_generatedToken_returnsEmailAndRoles() {
        String token = jwtService.generate("admin@orlo.co", List.of("ADMIN"));

        assertThat(jwtService.parse(token)).hasValueSatisfying(t -> {
            assertThat(t.email()).isEqualTo("admin@orlo.co");
            assertThat(t.roles()).containsExactly("ADMIN");
        });
    }

    @Test
    void parse_tokenWithoutExpiration_isRejected() {
        String token = Jwts.builder().issuer(JwtService.ISSUER).subject("admin@orlo.co")
                .claim("roles", List.of("ADMIN")).issuedAt(new Date()).signWith(KEY).compact();

        assertThat(jwtService.parse(token)).isEmpty();
    }

    @Test
    void parse_tokenWithoutIssuer_isRejected() {
        String token = Jwts.builder().subject("admin@orlo.co").claim("roles", List.of("ADMIN"))
                .expiration(new Date(System.currentTimeMillis() + 60_000)).signWith(KEY).compact();

        assertThat(jwtService.parse(token)).isEmpty();
    }

    @Test
    void parse_tokenFromOtherIssuer_isRejected() {
        String token = Jwts.builder().issuer("otra-api").subject("admin@orlo.co").claim("roles", List.of("ADMIN"))
                .expiration(new Date(System.currentTimeMillis() + 60_000)).signWith(KEY).compact();

        assertThat(jwtService.parse(token)).isEmpty();
    }

    @Test
    void parse_expiredToken_isRejected() {
        String token = Jwts.builder().issuer(JwtService.ISSUER).subject("admin@orlo.co").claim("roles", List.of("ADMIN"))
                .expiration(new Date(System.currentTimeMillis() - 60_000)).signWith(KEY).compact();

        assertThat(jwtService.parse(token)).isEmpty();
    }

    @Test
    void parse_unsignedToken_isRejected() {
        String token = Jwts.builder().issuer(JwtService.ISSUER).subject("admin@orlo.co").claim("roles", List.of("ADMIN"))
                .expiration(new Date(System.currentTimeMillis() + 60_000)).compact();

        assertThat(jwtService.parse(token)).isEmpty();
    }
}
