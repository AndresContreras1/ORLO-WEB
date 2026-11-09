package com.orlo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orlo.api.client.FirebaseAuthClient;
import com.orlo.api.client.FirebaseAuthClient.FirebaseIdentity;
import com.orlo.api.dto.QuizAnswerRequest;
import com.orlo.api.dto.QuizScoreRequest;
import com.orlo.api.dto.QuizScoreResponse;
import com.orlo.api.dto.StyleDto;
import com.orlo.api.service.AppUserService;
import com.orlo.api.service.QuizService;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QuizController.class)
@ActiveProfiles("test")
@Import(WebTestSecurity.class)
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuizService quizService;
    @MockitoBean
    private AppUserService appUserService;
    @MockitoBean
    private FirebaseAuthClient firebaseAuthClient;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(firebaseAuthClient.verify("token-app")).thenReturn(Optional.of(new FirebaseIdentity("uid-1", "a@b.co")));
    }

    @Test
    void score_validAnswers_returnsPrimaryAndSecondary() throws Exception {
        StyleDto urbano = new StyleDto(2L, "urbano", "Urbano", "desc", List.of("#000000"), List.of());
        StyleDto casual = new StyleDto(1L, "casual", "Casual", "desc", List.of("#FFFFFF"), List.of());
        when(appUserService.registerVisit(any())).thenReturn(false);
        when(quizService.score(eq("uid-1"), eq(false), any()))
                .thenReturn(new QuizScoreResponse(urbano, casual, Map.of("urbano", 5, "casual", 2)));
        var body = new QuizScoreRequest(List.of(new QuizAnswerRequest(1L, List.of(10L))));

        mockMvc.perform(post("/api/v1/quiz/score")
                        .header("Authorization", "Bearer token-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primary.code").value("urbano"))
                .andExpect(jsonPath("$.secondary.code").value("casual"))
                .andExpect(jsonPath("$.scores.urbano").value(5));
    }

    @Test
    void score_emptyAnswers_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/v1/quiz/score")
                        .header("Authorization", "Bearer token-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.errors.answers").exists());
    }

    @Test
    void active_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/quiz/active"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void active_invalidFirebaseToken_returns401() throws Exception {
        when(firebaseAuthClient.verify("vencido")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/quiz/active").header("Authorization", "Bearer vencido"))
                .andExpect(status().isUnauthorized());
    }
}
