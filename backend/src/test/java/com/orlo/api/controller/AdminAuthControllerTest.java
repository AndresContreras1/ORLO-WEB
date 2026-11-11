package com.orlo.api.controller;

import com.orlo.api.client.FirebaseAuthClient;
import com.orlo.api.controller.admin.AdminAuthController;
import com.orlo.api.service.impl.AdminAuthServiceImpl;
import com.orlo.api.service.impl.LoginAttemptServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Login del panel con la autenticación real (BCrypt) y el límite de intentos real.
 * Cada prueba arranca con los contadores en cero.
 */
@WebMvcTest(controllers = AdminAuthController.class)
@ActiveProfiles("test")
@Import({WebTestSecurity.class, AdminAuthServiceImpl.class, LoginAttemptServiceImpl.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminAuthControllerTest {

    private static final String PASSWORD = "Clave-Correcta-2026";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private FirebaseAuthClient firebaseAuthClient;

    @BeforeEach
    void setUp() {
        doThrow(new UsernameNotFoundException("no existe")).when(userDetailsService).loadUserByUsername(anyString());
        doReturn(User.withUsername("admin@orlo.co").password(passwordEncoder.encode(PASSWORD)).authorities("ADMIN").build())
                .when(userDetailsService).loadUserByUsername("admin@orlo.co");
    }

    @Test
    void login_correctPassword_returnsTokenWithoutPassword() throws Exception {
        login("admin@orlo.co", PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void login_sixthWrongPassword_returns429WithRetryAfter() throws Exception {
        for (int i = 0; i < 5; i++) {
            login("admin@orlo.co", "mala-" + i).andExpect(status().isUnauthorized());
        }

        login("admin@orlo.co", "mala-6")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"));
        // Mientras dura el bloqueo, ni la contraseña correcta entra
        login("admin@orlo.co", PASSWORD).andExpect(status().isTooManyRequests());
    }

    @Test
    void login_unknownEmail_sameAnswerAsWrongPassword() throws Exception {
        login("nadie@orlo.co", "mala")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Correo o contraseña incorrectos"));
        login("admin@orlo.co", "mala")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Correo o contraseña incorrectos"));
    }

    @Test
    void login_hugePassword_returns400BeforeHashing() throws Exception {
        login("admin@orlo.co", "a".repeat(5000))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }
}
