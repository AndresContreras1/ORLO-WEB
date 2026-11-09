package com.orlo.api.client;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.AnthropicException;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.orlo.api.exception.AiUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Cliente del API de Anthropic con salida estructurada: el modelo devuelve un JSON con la forma de
 * {@link ModelOutput}. La clave llega por la variable de entorno ORLO_ANTHROPIC_API_KEY.
 */
@Component
public class AnthropicAdvisorClient implements AdvisorAiClient {

    private static final Logger log = LoggerFactory.getLogger(AnthropicAdvisorClient.class);

    private final AnthropicClient client;

    public AnthropicAdvisorClient(@Value("${app.ai.api-key:}") String apiKey,
                                  @Value("${app.ai.timeout-seconds}") long timeoutSeconds) {
        this.client = apiKey == null || apiKey.isBlank()
                ? null
                : AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .maxRetries(1)
                .build();
        if (client == null) {
            log.warn("ORLO_ANTHROPIC_API_KEY no está definida: el asesor IA responderá 503");
        }
    }

    @Override
    public AiAnswer ask(AiQuestion question) {
        if (client == null) {
            throw new AiUnavailableException("El asesor no está disponible en este momento");
        }
        StructuredMessageCreateParams.Builder<ModelOutput> builder = MessageCreateParams.builder()
                .model(question.model())
                .maxTokens(question.maxOutputTokens())
                .system(question.systemPrompt())
                .outputConfig(ModelOutput.class);
        for (AiTurn turn : question.turns()) {
            if (turn.fromUser()) {
                builder.addUserMessage(turn.text());
            } else {
                builder.addAssistantMessage(turn.text());
            }
        }
        try {
            StructuredMessage<ModelOutput> message = client.messages().create(builder.build());
            if (message.stopReason().filter(StopReason.REFUSAL::equals).isPresent()) {
                throw new AiUnavailableException("El asesor no puede responder esa pregunta. Prueba con otra");
            }
            ModelOutput output = message.content().stream()
                    .flatMap(block -> block.text().stream())
                    .map(text -> text.text())
                    .findFirst()
                    .orElseThrow(() -> new AiUnavailableException("El asesor no devolvió una respuesta. Intenta de nuevo"));
            List<AiOutfit> outfits = output.outfits() == null ? List.of() : output.outfits().stream()
                    .map(o -> new AiOutfit(o.title(), o.items() == null ? List.of() : o.items(), o.why()))
                    .toList();
            String followUp = output.followUpQuestion() == null || output.followUpQuestion().isBlank()
                    ? null : output.followUpQuestion();
            return new AiAnswer(output.reply(), outfits, followUp,
                    message.usage().inputTokens(), message.usage().outputTokens());
        } catch (AnthropicException e) {
            log.error("Falló la llamada al modelo {}", question.model(), e);
            throw new AiUnavailableException("El asesor no está disponible en este momento. Intenta de nuevo");
        }
    }

    @Override
    public DailyAnswer dailyOutfit(AiQuestion question) {
        if (client == null) {
            throw new AiUnavailableException("El asesor no está disponible en este momento");
        }
        StructuredMessageCreateParams.Builder<ModelOutfit> builder = MessageCreateParams.builder()
                .model(question.model())
                .maxTokens(question.maxOutputTokens())
                .system(question.systemPrompt())
                .outputConfig(ModelOutfit.class);
        for (AiTurn turn : question.turns()) {
            if (turn.fromUser()) {
                builder.addUserMessage(turn.text());
            } else {
                builder.addAssistantMessage(turn.text());
            }
        }
        try {
            StructuredMessage<ModelOutfit> message = client.messages().create(builder.build());
            ModelOutfit outfit = message.content().stream()
                    .flatMap(block -> block.text().stream())
                    .map(text -> text.text())
                    .findFirst()
                    .orElseThrow(() -> new AiUnavailableException("El asesor no devolvió el outfit del día"));
            return new DailyAnswer(new AiOutfit(outfit.title(), outfit.items() == null ? List.of() : outfit.items(),
                    outfit.why()), message.usage().inputTokens(), message.usage().outputTokens());
        } catch (AnthropicException e) {
            log.error("Falló el outfit del día con el modelo {}", question.model(), e);
            throw new AiUnavailableException("El asesor no está disponible en este momento. Intenta de nuevo");
        }
    }

    /** Forma del JSON que se le pide al modelo. */
    record ModelOutput(
            @JsonPropertyDescription("Respuesta corta y cercana para la persona, en español, sin jerga de moda")
            String reply,
            @JsonPropertyDescription("De 2 a 3 opciones de outfit; vacío si primero hay que hacer una pregunta")
            List<ModelOutfit> outfits,
            @JsonPropertyDescription("Una sola pregunta si falta información (ocasión, clima); cadena vacía si no hace falta")
            String followUpQuestion) {
    }

    record ModelOutfit(
            @JsonPropertyDescription("Nombre corto del outfit") String title,
            @JsonPropertyDescription("Prendas del outfit, priorizando las que la persona ya tiene") List<String> items,
            @JsonPropertyDescription("Por qué funciona, en una o dos líneas") String why) {
    }
}
