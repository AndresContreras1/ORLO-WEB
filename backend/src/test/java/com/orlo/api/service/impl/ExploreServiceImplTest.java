package com.orlo.api.service.impl;

import com.orlo.api.dto.CompleteWardrobeRequest;
import com.orlo.api.dto.ExploreItemDto;
import com.orlo.api.dto.ExploreSection;
import com.orlo.api.dto.WardrobeItem;
import com.orlo.api.dto.WardrobeSuggestionDto;
import com.orlo.api.entities.ExploreItem;
import com.orlo.api.exception.PremiumRequiredException;
import com.orlo.api.repository.ExploreItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExploreServiceImplTest {

    @Mock
    private ExploreItemRepository exploreItemRepository;

    @InjectMocks
    private ExploreServiceImpl exploreService;

    private ExploreItem jeanUrbano;
    private ExploreItem camisaClasica;
    private ExploreItem gorraTendencia;
    private ExploreItem chaqueta;
    private ExploreItem camisetaRoja;

    @BeforeEach
    void setUp() {
        jeanUrbano = item(1L, "Jean recto", "abajo", "azul", Set.of("urbano", "casual"), false, false);
        camisaClasica = item(2L, "Camisa blanca", "arriba", "blanco", Set.of("clasico"), false, false);
        gorraTendencia = item(3L, "Gorra", "accesorio", "negro", Set.of("urbano"), true, true);
        chaqueta = item(4L, "Chaqueta de jean", "abrigo", "azul", Set.of("urbano"), false, false);
        camisetaRoja = item(5L, "Camiseta roja", "arriba", "rojo", Set.of("creativo"), true, false);
    }

    @Test
    void items_forYourStyle_onlyStyleAndNoAccessories() {
        when(exploreItemRepository.findByActiveTrueOrderByNameAsc())
                .thenReturn(List.of(jeanUrbano, camisaClasica, gorraTendencia, chaqueta));

        List<ExploreItemDto> items = exploreService.items(ExploreSection.FOR_YOUR_STYLE, "Urbano", true);

        assertThat(items).extracting(ExploreItemDto::name).containsExactly("Jean recto", "Chaqueta de jean");
    }

    @Test
    void items_styleWithoutMatches_returnsWholeSection() {
        when(exploreItemRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(jeanUrbano, camisaClasica));

        List<ExploreItemDto> items = exploreService.items(ExploreSection.FOR_YOUR_STYLE, "deportivo", true);

        assertThat(items).hasSize(2);
    }

    @Test
    void items_trendsAndAccessories_useTheirFlags() {
        when(exploreItemRepository.findByActiveTrueOrderByNameAsc())
                .thenReturn(List.of(jeanUrbano, gorraTendencia, camisetaRoja));

        assertThat(exploreService.items(ExploreSection.TRENDS, null, true))
                .extracting(ExploreItemDto::id).containsExactly(3L, 5L);
        assertThat(exploreService.items(ExploreSection.ACCESSORIES, null, true))
                .extracting(ExploreItemDto::id).containsExactly(3L);
    }

    @Test
    void items_notPremium_throwsPremiumRequired() {
        assertThatThrownBy(() -> exploreService.items(ExploreSection.TRENDS, null, false))
                .isInstanceOf(PremiumRequiredException.class);
    }

    @Test
    void completeWardrobe_noCoatAndAllNeutral_suggestsCoatAndColor() {
        when(exploreItemRepository.findByActiveTrueOrderByNameAsc())
                .thenReturn(List.of(jeanUrbano, camisaClasica, chaqueta, camisetaRoja, gorraTendencia));
        var wardrobe = List.of(
                new WardrobeItem("Camiseta", "arriba", "blanco"),
                new WardrobeItem("Camiseta", "arriba", "negro"),
                new WardrobeItem("Buzo", "arriba", "gris"),
                new WardrobeItem("Jean", "abajo", "negro"),
                new WardrobeItem("Pantalón", "abajo", "beige"),
                new WardrobeItem("Tenis", "calzado", "blanco"),
                new WardrobeItem("Botas", "calzado", "negro"),
                new WardrobeItem("Reloj", "accesorio", "gris"));

        List<WardrobeSuggestionDto> result = exploreService.completeWardrobe(
                new CompleteWardrobeRequest("urbano", wardrobe), true);

        assertThat(result).extracting(WardrobeSuggestionDto::category).containsExactly("abrigo", "arriba");
        assertThat(result.get(0).items()).extracting(ExploreItemDto::name).containsExactly("Chaqueta de jean");
        assertThat(result.get(1).title()).isEqualTo("Una prenda con color");
        assertThat(result.get(1).items()).extracting(ExploreItemDto::color).containsOnly("rojo");
    }

    @Test
    void completeWardrobe_emptyWardrobe_atMostFourSuggestionsWithItems() {
        when(exploreItemRepository.findByActiveTrueOrderByNameAsc())
                .thenReturn(List.of(jeanUrbano, camisaClasica, chaqueta, gorraTendencia));

        List<WardrobeSuggestionDto> result = exploreService.completeWardrobe(
                new CompleteWardrobeRequest(null, null), true);

        assertThat(result).hasSizeLessThanOrEqualTo(4).allSatisfy(s -> assertThat(s.items()).isNotEmpty());
    }

    @Test
    void normalizeStyle_nameWithAccent_returnsCode() {
        assertThat(ExploreServiceImpl.normalizeStyle(" Clásico ")).isEqualTo("clasico");
        assertThat(ExploreServiceImpl.normalizeStyle("")).isNull();
    }

    private static ExploreItem item(Long id, String name, String category, String color, Set<String> styles,
                                    boolean trend, boolean accessory) {
        ExploreItem item = new ExploreItem();
        item.setId(id);
        item.setName(name);
        item.setImageUrl("https://img/" + id + ".jpg");
        item.setCategory(category);
        item.setColor(color);
        item.setStyles(new java.util.HashSet<>(styles));
        item.setWhyItFits("Encaja");
        item.setTrend(trend);
        item.setAccessory(accessory);
        return item;
    }
}
