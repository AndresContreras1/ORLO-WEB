package com.orlo.api.service.impl;

import com.orlo.api.client.AdvisorAiClient;
import com.orlo.api.client.AdvisorAiClient.AiAnswer;
import com.orlo.api.client.AdvisorAiClient.AiOutfit;
import com.orlo.api.client.AdvisorAiClient.AiQuestion;
import com.orlo.api.dto.AdvisorMessageRequest;
import com.orlo.api.dto.AdvisorProfile;
import com.orlo.api.dto.AdvisorReplyDto;
import com.orlo.api.dto.ChatRole;
import com.orlo.api.dto.HistoryItem;
import com.orlo.api.dto.QuickAction;
import com.orlo.api.dto.WardrobeItem;
import com.orlo.api.dto.admin.AiConfigDto;
import com.orlo.api.exception.AiUnavailableException;
import com.orlo.api.exception.LimitReachedException;
import com.orlo.api.exception.PremiumRequiredException;
import com.orlo.api.security.AppPrincipal;
import com.orlo.api.service.AiConfigService;
import com.orlo.api.service.AppUserService;
import com.orlo.api.service.UsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvisorServiceImplTest {

    private static final AppPrincipal PRINCIPAL = new AppPrincipal("uid-1", "ana@correo.co");
    private static final AiConfigDto CONFIG = new AiConfigDto("Reglas de ORLO", "claude-haiku-5-5", 20, 1200);

    @Mock
    private AppUserService appUserService;
    @Mock
    private UsageService usageService;
    @Mock
    private AiConfigService aiConfigService;
    @Mock
    private AdvisorAiClient aiClient;

    @InjectMocks
    private AdvisorServiceImpl advisorService;

    private AdvisorMessageRequest request;

    @BeforeEach
    void setUp() {
        request = new AdvisorMessageRequest("¿Qué me pongo para la universidad?", QuickAction.MORE_CASUAL,
                new AdvisorProfile("Ana María Pérez", "18-24", "femenino", "Urbano", "Creativo", null, null, null,
                        "Cálido", null, List.of("Estudio")),
                List.of(new WardrobeItem("Jean azul", "Parte de abajo", "Azul")),
                List.of(new HistoryItem(ChatRole.ASSISTANT, "Hola"), new HistoryItem(ChatRole.USER, "Hola")));
    }

    @Test
    void ask_notPremium_throwsPremiumRequiredWithoutSpending() {
        when(appUserService.registerVisit(PRINCIPAL)).thenReturn(false);

        assertThatThrownBy(() -> advisorService.ask(PRINCIPAL, request)).isInstanceOf(PremiumRequiredException.class);
        verify(usageService, never()).reserve(anyString(), org.mockito.ArgumentMatchers.anyInt());
        verify(aiClient, never()).ask(any());
    }

    @Test
    void ask_limitReached_doesNotCallModel() {
        when(appUserService.registerVisit(PRINCIPAL)).thenReturn(true);
        when(aiConfigService.get()).thenReturn(CONFIG);
        when(usageService.reserve("uid-1", 20)).thenThrow(new LimitReachedException(20));

        assertThatThrownBy(() -> advisorService.ask(PRINCIPAL, request)).isInstanceOf(LimitReachedException.class);
        verify(aiClient, never()).ask(any());
    }

    @Test
    void ask_modelFails_releasesReservedMessage() {
        when(appUserService.registerVisit(PRINCIPAL)).thenReturn(true);
        when(aiConfigService.get()).thenReturn(CONFIG);
        when(usageService.reserve("uid-1", 20)).thenReturn(19);
        when(aiClient.ask(any())).thenThrow(new AiUnavailableException("caído"));

        assertThatThrownBy(() -> advisorService.ask(PRINCIPAL, request)).isInstanceOf(AiUnavailableException.class);
        verify(usageService).release("uid-1");
        verify(usageService, never()).recordTokens(anyString(), anyString(), anyLong(), anyLong());
    }

    @Test
    void ask_success_returnsOutfitsAndRecordsTokens() {
        // Arrange
        when(appUserService.registerVisit(PRINCIPAL)).thenReturn(true);
        when(aiConfigService.get()).thenReturn(CONFIG);
        when(usageService.reserve("uid-1", 20)).thenReturn(19);
        when(aiClient.ask(any())).thenReturn(new AiAnswer("Te propongo esto", List.of(
                new AiOutfit("Básico", List.of("Jean azul", "Camiseta"), "Es fresco")), null, 900, 300));

        // Act
        AdvisorReplyDto reply = advisorService.ask(PRINCIPAL, request);

        // Assert
        assertThat(reply.reply()).isEqualTo("Te propongo esto");
        assertThat(reply.outfits()).hasSize(1);
        assertThat(reply.remainingMessages()).isEqualTo(19);
        verify(usageService).recordTokens("uid-1", "claude-haiku-5-5", 900, 300);
        ArgumentCaptor<AiQuestion> question = ArgumentCaptor.forClass(AiQuestion.class);
        verify(aiClient).ask(question.capture());
        assertThat(question.getValue().systemPrompt())
                .contains("Reglas de ORLO", "Nombre: Ana\n", "Jean azul", "Estudio")
                .doesNotContain("Pérez", "ana@correo.co");
    }

    @Test
    void buildTurns_historyStartingWithAssistant_startsWithUserAndAddsQuickAction() {
        var turns = AdvisorServiceImpl.buildTurns(request);

        assertThat(turns.get(0).fromUser()).isTrue();
        assertThat(turns).hasSize(1);
        assertThat(turns.get(0).text()).contains("Hola", "universidad", "más casual");
    }

    @Test
    void firstName_fullName_onlyFirst() {
        assertThat(AdvisorServiceImpl.firstName("  Ana María Pérez ")).isEqualTo("Ana");
        assertThat(AdvisorServiceImpl.firstName(" ")).isNull();
    }
}
