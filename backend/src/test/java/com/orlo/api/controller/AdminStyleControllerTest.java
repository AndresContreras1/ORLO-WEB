package com.orlo.api.controller;

import com.orlo.api.client.FirebaseAuthClient;
import com.orlo.api.controller.admin.AdminStyleController;
import com.orlo.api.dto.StyleDto;
import com.orlo.api.exception.BusinessRuleException;
import com.orlo.api.security.JwtService;
import com.orlo.api.service.StyleService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminStyleController.class)
@ActiveProfiles("test")
@Import(WebTestSecurity.class)
class AdminStyleControllerTest {

    private static final String VALID = """
            {"code":"urbano","name":"Urbano","description":"Ropa de ciudad","palette":["#112233"],"gallery":[]}
            """;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private StyleService styleService;
    @MockitoBean
    private FirebaseAuthClient firebaseAuthClient;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        adminToken = jwtService.generate("admin@orlo.co", List.of("ADMIN"));
    }

    @Test
    void create_valid_returns201WithLocation() throws Exception {
        when(styleService.create(any())).thenReturn(
                new StyleDto(7L, "urbano", "Urbano", "Ropa de ciudad", List.of("#112233"), List.of()));

        mockMvc.perform(post("/api/v1/admin/styles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/admin/styles/7"))
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void create_badHexColor_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/admin/styles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID.replace("#112233", "rojo")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"));
    }

    @Test
    void create_javascriptImageUrl_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/admin/styles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID.replace("\"gallery\":[]", "\"gallery\":[\"javascript:alert(1)\"]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"));
    }

    @Test
    void delete_styleInUse_returns409() throws Exception {
        doThrow(new BusinessRuleException("en uso")).when(styleService).delete(3L);

        mockMvc.perform(delete("/api/v1/admin/styles/3").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE"));
    }

    @Test
    void delete_ok_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/styles/3").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}
