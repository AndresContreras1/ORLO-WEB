package com.orlo.api.service.impl;

import com.orlo.api.client.AdvisorAiClient;
import com.orlo.api.client.AdvisorAiClient.AiAnswer;
import com.orlo.api.client.AdvisorAiClient.AiQuestion;
import com.orlo.api.client.AdvisorAiClient.AiTurn;
import com.orlo.api.client.AdvisorAiClient.DailyAnswer;
import com.orlo.api.dto.AdvisorMessageRequest;
import com.orlo.api.dto.AdvisorProfile;
import com.orlo.api.dto.AdvisorReplyDto;
import com.orlo.api.dto.ChatRole;
import com.orlo.api.dto.DailyOutfitDto;
import com.orlo.api.dto.DailyOutfitRequest;
import com.orlo.api.dto.HistoryItem;
import com.orlo.api.dto.OutfitDto;
import com.orlo.api.dto.QuickAction;
import com.orlo.api.dto.WardrobeItem;
import com.orlo.api.dto.admin.AiConfigDto;
import com.orlo.api.exception.PremiumRequiredException;
import com.orlo.api.security.AppPrincipal;
import com.orlo.api.service.AdvisorService;
import com.orlo.api.service.AiConfigService;
import com.orlo.api.service.AppUserService;
import com.orlo.api.service.UsageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Sin @Transactional a propósito: la llamada al modelo tarda segundos y no debe ocupar
 * una conexión de la base de datos. Cada paso con datos abre su propia transacción.
 */
@Service
public class AdvisorServiceImpl implements AdvisorService {

    private final AppUserService appUserService;
    private final UsageService usageService;
    private final AiConfigService aiConfigService;
    private final AdvisorAiClient aiClient;
    private final int dailyOutfitLimit;

    public AdvisorServiceImpl(AppUserService appUserService, UsageService usageService,
                              AiConfigService aiConfigService, AdvisorAiClient aiClient,
                              @Value("${app.advisor.daily-outfit-limit:3}") int dailyOutfitLimit) {
        this.appUserService = appUserService;
        this.usageService = usageService;
        this.aiConfigService = aiConfigService;
        this.aiClient = aiClient;
        this.dailyOutfitLimit = dailyOutfitLimit;
    }

    @Override
    public DailyOutfitDto dailyOutfit(AppPrincipal principal, DailyOutfitRequest request) {
        if (!appUserService.registerVisit(principal)) {
            throw new PremiumRequiredException();
        }
        usageService.reserveDailyOutfit(principal.uid(), dailyOutfitLimit);
        AiConfigDto config = aiConfigService.get();
        AdvisorMessageRequest asMessage = new AdvisorMessageRequest(DAILY_OUTFIT_QUESTION, null, request.profile(),
                request.wardrobeOrEmpty(), List.of());
        String system = buildSystemPrompt(config.systemPrompt(), asMessage) + DAILY_OUTFIT_RULES;
        DailyAnswer answer = aiClient.dailyOutfit(new AiQuestion(config.model(), system, config.maxOutputTokens(),
                List.of(new AiTurn(true, DAILY_OUTFIT_QUESTION))));
        usageService.recordTokens(principal.uid(), config.model(), answer.inputTokens(), answer.outputTokens());
        return new DailyOutfitDto(answer.outfit().title(), answer.outfit().items(), answer.outfit().why());
    }

    static final String DAILY_OUTFIT_QUESTION = "¿Qué me pongo hoy?";

    static final String DAILY_OUTFIT_RULES = "\n## Outfit del día\n"
            + "Propón un solo outfit para hoy, pensado para el clima y las ocasiones del perfil. "
            + "Usa primero las prendas que ya tiene, con el nombre tal como aparece en la lista. "
            + "Si no tiene prendas registradas, propón un outfit sencillo con prendas básicas. "
            + "En \"why\" explica en una o dos líneas por qué funciona.\n";

    @Override
    public AdvisorReplyDto ask(AppPrincipal principal, AdvisorMessageRequest request) {
        if (!appUserService.registerVisit(principal)) {
            throw new PremiumRequiredException();
        }
        AiConfigDto config = aiConfigService.get();
        int remaining = usageService.reserve(principal.uid(), config.dailyLimit());

        AiAnswer answer;
        try {
            answer = aiClient.ask(new AiQuestion(config.model(), buildSystemPrompt(config.systemPrompt(), request),
                    config.maxOutputTokens(), buildTurns(request)));
        } catch (RuntimeException e) {
            usageService.release(principal.uid());
            throw e;
        }
        usageService.recordTokens(principal.uid(), config.model(), answer.inputTokens(), answer.outputTokens());

        List<OutfitDto> outfits = answer.outfits().stream()
                .map(o -> new OutfitDto(o.title(), o.items(), o.why()))
                .toList();
        return new AdvisorReplyDto(answer.reply(), outfits, answer.followUpQuestion(), remaining);
    }

    /** Reglas fijas del panel + el perfil y el armario de la persona. */
    static String buildSystemPrompt(String rules, AdvisorMessageRequest request) {
        AdvisorProfile p = request.profile();
        StringBuilder sb = new StringBuilder(rules.trim()).append("\n\n## Perfil de la persona\n");
        line(sb, "Nombre", firstName(p.name()));
        line(sb, "Rango de edad", p.ageRange());
        line(sb, "Cómo prefiere que se hable de sus prendas", p.garmentPronoun());
        line(sb, "Estilo principal", p.primaryStyle());
        line(sb, "Estilo secundario", p.secondaryStyle());
        line(sb, "Tono de piel", p.skinTone());
        line(sb, "Color de cabello", p.hairColor());
        line(sb, "Color de ojos", p.eyeColor());
        line(sb, "Clima habitual", p.climate());
        line(sb, "Presupuesto", p.budget());
        if (p.occasions() != null && !p.occasions().isEmpty()) {
            line(sb, "Ocasiones principales", String.join(", ", p.occasions()));
        }
        sb.append("\n## Prendas que ya tiene\n");
        List<WardrobeItem> wardrobe = request.wardrobeOrEmpty();
        if (wardrobe.isEmpty()) {
            sb.append("No ha registrado prendas todavía.\n");
        } else {
            for (WardrobeItem item : wardrobe) {
                sb.append("- ").append(nonBlank(item.name(), "Prenda"))
                        .append(" (").append(nonBlank(item.category(), "sin categoría"))
                        .append(", ").append(nonBlank(item.color(), "sin color")).append(")\n");
            }
        }
        return sb.toString();
    }

    /**
     * El historial va en orden y debe empezar por la persona y alternar turnos;
     * los turnos seguidos del mismo lado se unen.
     */
    static List<AiTurn> buildTurns(AdvisorMessageRequest request) {
        List<AiTurn> turns = new ArrayList<>();
        for (HistoryItem item : request.historyOrEmpty()) {
            boolean fromUser = item.role() == ChatRole.USER;
            if (turns.isEmpty() && !fromUser) {
                continue;
            }
            addTurn(turns, fromUser, item.text());
        }
        addTurn(turns, true, request.message().trim() + quickActionHint(request.quickAction()));
        return turns;
    }

    private static void addTurn(List<AiTurn> turns, boolean fromUser, String text) {
        if (!turns.isEmpty() && turns.get(turns.size() - 1).fromUser() == fromUser) {
            AiTurn last = turns.remove(turns.size() - 1);
            turns.add(new AiTurn(fromUser, last.text() + "\n\n" + text));
        } else {
            turns.add(new AiTurn(fromUser, text));
        }
    }

    private static String quickActionHint(QuickAction action) {
        if (action == null) {
            return "";
        }
        return switch (action) {
            case MORE_CASUAL -> "\n\n(Ajusta tu última propuesta para que sea más casual.)";
            case MORE_FORMAL -> "\n\n(Ajusta tu última propuesta para que sea más formal.)";
            case ANOTHER -> "\n\n(Dame otra opción distinta a las anteriores.)";
        };
    }

    /** Solo el primer nombre: no se envían apellidos al servicio externo. */
    static String firstName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return name.trim().split("\\s+")[0];
    }

    private static void line(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append("- ").append(label).append(": ").append(value.trim()).append('\n');
        }
    }

    private static String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
