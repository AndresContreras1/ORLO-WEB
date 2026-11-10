package com.orlo.api.security;

import com.orlo.api.client.FirebaseAuthClient;
import com.orlo.api.client.FirebaseAuthClient.FirebaseIdentity;
import com.orlo.api.controller.MeController;
import com.orlo.api.controller.WebTestSecurity;
import com.orlo.api.dto.MeDto;
import com.orlo.api.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Cabeceras de seguridad, límite de peticiones por usuario y tamaño máximo del cuerpo. */
@WebMvcTest(controllers = MeController.class)
@ActiveProfiles("test")
@Import(WebTestSecurity.class)
@TestPropertySource(properties = {"app.rate-limit.requests-per-minute=3", "app.http.max-body-bytes=1000"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProteccionesHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private FirebaseAuthClient firebaseAuthClient;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(firebaseAuthClient.verify(anyString())).thenReturn(Optional.empty());
        when(firebaseAuthClient.verify("token-a")).thenReturn(Optional.of(new FirebaseIdentity("uid-a", "a@b.co")));
        when(firebaseAuthClient.verify("token-b")).thenReturn(Optional.of(new FirebaseIdentity("uid-b", "b@b.co")));
        when(accountService.me(any())).thenReturn(new MeDto("uid-a", false, 20, 20));
    }

    @Test
    void apiResponse_hasCspAndReferrerPolicy() throws Exception {
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer token-a"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", SecurityConfig.API_CSP))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    void sameUser_overTheLimit_gets429WithRetryAfter() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer token-a")).andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer token-a"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"));
        // Otro usuario tiene su propio cupo
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer token-b")).andExpect(status().isOk());
    }

    @Test
    void bodyOverTheMaximum_returns413() throws Exception {
        mockMvc.perform(post("/api/v1/me")
                        .header("Authorization", "Bearer token-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"x\":\"" + "a".repeat(2000) + "\"}"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value("PAYLOAD_TOO_LARGE"));
    }
}
