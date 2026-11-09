package com.orlo.api.controller;

import com.orlo.api.client.FirebaseAuthClient;
import com.orlo.api.client.FirebaseAuthClient.FirebaseIdentity;
import com.orlo.api.dto.ExploreItemDto;
import com.orlo.api.dto.ExploreSection;
import com.orlo.api.exception.PremiumRequiredException;
import com.orlo.api.service.AppUserService;
import com.orlo.api.service.ExploreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExploreController.class)
@ActiveProfiles("test")
@Import(WebTestSecurity.class)
class ExploreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExploreService exploreService;
    @MockitoBean
    private AppUserService appUserService;
    @MockitoBean
    private FirebaseAuthClient firebaseAuthClient;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(firebaseAuthClient.verify("token-app")).thenReturn(Optional.of(new FirebaseIdentity("uid-1", "a@b.co")));
        when(appUserService.registerVisit(any())).thenReturn(true);
    }

    @Test
    void items_validSection_returnsItems() throws Exception {
        when(exploreService.items(eq(ExploreSection.TRENDS), eq("urbano"), eq(true))).thenReturn(List.of(
                new ExploreItemDto(1L, "Pantalón cargo", "https://img/1.jpg", null, "abajo", "verde",
                        List.of("urbano"), "Da un aire relajado", true, false)));

        mockMvc.perform(get("/api/v1/explore/items").param("section", "TRENDS").param("style", "urbano")
                        .header("Authorization", "Bearer token-app"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("abajo"))
                .andExpect(jsonPath("$[0].trend").value(true));
    }

    @Test
    void items_unknownSection_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/explore/items").param("section", "OTRA")
                        .header("Authorization", "Bearer token-app"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void item_notPremium_returns403() throws Exception {
        when(exploreService.item(eq(9L), anyBoolean())).thenThrow(new PremiumRequiredException());

        mockMvc.perform(get("/api/v1/explore/items/9").header("Authorization", "Bearer token-app"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PREMIUM_REQUIRED"));
    }
}
