package com.orlo.api.security;

import com.orlo.api.client.FirebaseAuthClient;
import com.orlo.api.client.FirebaseAuthClient.FirebaseIdentity;
import com.orlo.api.controller.MeController;
import com.orlo.api.controller.WebTestSecurity;
import com.orlo.api.controller.admin.AdminMetricsController;
import com.orlo.api.dto.MeDto;
import com.orlo.api.dto.admin.MetricsDto;
import com.orlo.api.service.AccountService;
import com.orlo.api.service.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Reglas de acceso: quién entra al panel y quién a los endpoints de la app. */
@WebMvcTest(controllers = {AdminMetricsController.class, MeController.class})
@ActiveProfiles("test")
@Import(WebTestSecurity.class)
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private MetricsService metricsService;
    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private FirebaseAuthClient firebaseAuthClient;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(firebaseAuthClient.verify(anyString())).thenReturn(Optional.empty());
        when(firebaseAuthClient.verify("token-app")).thenReturn(Optional.of(new FirebaseIdentity("uid-1", "a@b.co")));
        when(metricsService.current()).thenReturn(new MetricsDto(1, 0, 0, 0, 0, 0, BigDecimal.ZERO, 6, 5));
        when(accountService.me(any())).thenReturn(new MeDto("uid-1", false, 20, 0));
    }

    @Test
    void adminRoute_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/metrics"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void adminRoute_adminToken_returns200() throws Exception {
        String token = jwtService.generate("admin@orlo.co", List.of("ADMIN"));

        mockMvc.perform(get("/api/v1/admin/metrics").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishedLessons").value(6));
    }

    @Test
    void adminRoute_tokenWithoutAdminRole_returns403() throws Exception {
        String token = jwtService.generate("editor@orlo.co", List.of("EDITOR"));

        mockMvc.perform(get("/api/v1/admin/metrics").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminRoute_tamperedToken_returns401() throws Exception {
        String token = jwtService.generate("admin@orlo.co", List.of("ADMIN"));
        String tampered = token.substring(0, token.length() - 3) + "abc";

        mockMvc.perform(get("/api/v1/admin/metrics").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminRoute_appUserToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/metrics").header("Authorization", "Bearer token-app"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void appRoute_adminToken_returns401() throws Exception {
        String token = jwtService.generate("admin@orlo.co", List.of("ADMIN"));

        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void appRoute_firebaseToken_returns200WithoutPassword() throws Exception {
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer token-app"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("uid-1"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void appRoute_withoutBearerPrefix_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/me").header("Authorization", "token-app"))
                .andExpect(status().isUnauthorized());
    }
}
