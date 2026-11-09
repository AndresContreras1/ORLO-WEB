package com.orlo.api.client;

import java.util.List;

/** Modelo de lenguaje que responde al asesor. Interfaz para poder simularlo en las pruebas. */
public interface AdvisorAiClient {

    AiAnswer ask(AiQuestion question);

    /** Un solo outfit para hoy (outfit del día). */
    DailyAnswer dailyOutfit(AiQuestion question);

    record AiTurn(boolean fromUser, String text) {
    }

    record AiQuestion(String model, String systemPrompt, int maxOutputTokens, List<AiTurn> turns) {
    }

    record AiOutfit(String title, List<String> items, String why) {
    }

    record AiAnswer(String reply, List<AiOutfit> outfits, String followUpQuestion,
                    long inputTokens, long outputTokens) {
    }

    record DailyAnswer(AiOutfit outfit, long inputTokens, long outputTokens) {
    }
}
