package com.orlo.api.client;

import com.orlo.api.client.AdvisorAiClient.AiOutfit;
import com.orlo.api.client.AnthropicAdvisorClient.ModelOutfit;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/** Lo que devuelve el modelo se recorta a los topes de Firestore antes de llegar a la app y la web. */
class AnthropicAdvisorClientTest {

    @Test
    void aOutfit_longTitle_isCutTo40() {
        AiOutfit outfit = AnthropicAdvisorClient.aOutfit(new ModelOutfit(
                "Un outfit urbano y relajado para una tarde de museos con amigos", List.of("Jean"), "Combina"));

        assertThat(outfit.title()).hasSizeLessThanOrEqualTo(AnthropicAdvisorClient.MAX_TITLE)
                .isEqualTo("Un outfit urbano y relajado para una tar");
    }

    @Test
    void aOutfit_shortValues_stayTheSame() {
        AiOutfit outfit = AnthropicAdvisorClient.aOutfit(new ModelOutfit("Para la U", List.of("Jean azul"), "Básicos"));

        assertThat(outfit.title()).isEqualTo("Para la U");
        assertThat(outfit.items()).containsExactly("Jean azul");
        assertThat(outfit.why()).isEqualTo("Básicos");
    }

    @Test
    void aOutfit_tooManyOrEmptyItems_areTrimmed() {
        List<String> items = IntStream.rangeClosed(1, 15).mapToObj(i -> i == 2 ? " " : "Prenda " + i).toList();

        AiOutfit outfit = AnthropicAdvisorClient.aOutfit(new ModelOutfit("Título", items, null));

        assertThat(outfit.items()).hasSize(AnthropicAdvisorClient.MAX_ITEMS).doesNotContain(" ");
        assertThat(outfit.why()).isNull();
    }

    @Test
    void recortar_doesNotSplitAnEmoji() {
        char[] relleno = new char[39];
        Arrays.fill(relleno, 'a');
        // El emoji ocupa las posiciones 39 y 40: cortar en 40 lo partiría por la mitad
        String texto = new String(relleno) + "👕 y más";

        String recortado = AnthropicAdvisorClient.recortar(texto, 40);

        assertThat(recortado).isEqualTo(new String(relleno));
        assertThat(Character.isHighSurrogate(recortado.charAt(recortado.length() - 1))).isFalse();
    }
}
