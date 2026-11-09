package com.orlo.api.controller;

import com.orlo.api.client.FirebaseAuthClient;
import com.orlo.api.client.FirebaseAuthClient.FirebaseIdentity;
import com.orlo.api.dto.AdvisorReplyDto;
import com.orlo.api.dto.OutfitDto;
import com.orlo.api.exception.LimitReachedException;
import com.orlo.api.exception.PremiumRequiredException;
import com.orlo.api.service.AdvisorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdvisorController.class)
@ActiveProfiles("test")
@Import(WebTestSecurity.class)
class AdvisorControllerTest {

    private static final String BODY = """
            {"message":"¿Qué me pongo?","profile":{"name":"Ana","occasions":["Estudio"]}}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdvisorService advisorService;
    @MockitoBean
    private FirebaseAuthClient firebaseAuthClient;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(firebaseAuthClient.verify("token-app")).thenReturn(Optional.of(new FirebaseIdentity("uid-1", "a@b.co")));
    }

    @Test
    void ask_premiumUser_returnsOutfits() throws Exception {
        when(advisorService.ask(any(), any())).thenReturn(new AdvisorReplyDto("Listo",
                List.of(new OutfitDto("Básico", List.of("Jean"), "Fresco")), null, 19));

        mockMvc.perform(request(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outfits[0].title").value("Básico"))
                .andExpect(jsonPath("$.remainingMessages").value(19));
    }

    @Test
    void ask_notPremium_returns403PremiumRequired() throws Exception {
        when(advisorService.ask(any(), any())).thenThrow(new PremiumRequiredException());

        mockMvc.perform(request(BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PREMIUM_REQUIRED"));
    }

    @Test
    void ask_limitReached_returns429LimitReached() throws Exception {
        when(advisorService.ask(any(), any())).thenThrow(new LimitReachedException(20));

        mockMvc.perform(request(BODY))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("LIMIT_REACHED"));
    }

    @Test
    void ask_blankMessage_returns400() throws Exception {
        mockMvc.perform(request("{\"message\":\" \",\"profile\":{}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").value("Escribe tu pregunta"));
    }

    private static MockHttpServletRequestBuilder request(String body) {
        return post("/api/v1/advisor/messages")
                .header("Authorization", "Bearer token-app")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }
}
