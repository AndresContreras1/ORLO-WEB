package com.orlo.api.service.impl;

import com.orlo.api.dto.CombineIdeaDto;
import com.orlo.api.dto.CompleteWardrobeRequest;
import com.orlo.api.dto.ExploreItemDetailDto;
import com.orlo.api.dto.ExploreItemDto;
import com.orlo.api.dto.ExploreSection;
import com.orlo.api.dto.WardrobeItem;
import com.orlo.api.dto.WardrobeSuggestionDto;
import com.orlo.api.dto.admin.AdminExploreItemDto;
import com.orlo.api.dto.admin.ExploreItemRequest;
import com.orlo.api.entities.CombineIdea;
import com.orlo.api.entities.ExploreItem;
import com.orlo.api.exception.NotFoundException;
import com.orlo.api.exception.PremiumRequiredException;
import com.orlo.api.repository.ExploreItemRepository;
import com.orlo.api.service.ExploreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ExploreServiceImpl implements ExploreService {

    /** Colores que combinan con todo: si el armario es solo esto, se sugiere una prenda con color. */
    static final Set<String> NEUTRALS = Set.of("negro", "blanco", "gris", "beige");
    private static final int SUGGESTED_ITEMS = 4;
    private static final int MAX_SUGGESTIONS = 4;

    private final ExploreItemRepository exploreItemRepository;

    public ExploreServiceImpl(ExploreItemRepository exploreItemRepository) {
        this.exploreItemRepository = exploreItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExploreItemDto> items(ExploreSection section, String style, boolean premium) {
        requirePremium(premium);
        String code = normalizeStyle(style);
        List<ExploreItem> active = exploreItemRepository.findByActiveTrueOrderByNameAsc();
        Predicate<ExploreItem> inSection = switch (section) {
            case FOR_YOUR_STYLE -> item -> !item.isAccessory();
            case TRENDS -> ExploreItem::isTrend;
            case ACCESSORIES -> ExploreItem::isAccessory;
        };
        List<ExploreItem> candidates = active.stream().filter(inSection).toList();
        if (code == null) {
            return candidates.stream().map(ExploreItemDto::from).toList();
        }
        // Primero lo que encaja con el estilo; si no hay nada de ese estilo, se muestra todo para no dejar la pestaña vacía
        List<ExploreItem> ofStyle = candidates.stream().filter(i -> i.getStyles().contains(code)).toList();
        return (ofStyle.isEmpty() ? candidates : ofStyle).stream().map(ExploreItemDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExploreItemDetailDto item(Long id, boolean premium) {
        requirePremium(premium);
        return ExploreItemDetailDto.from(exploreItemRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Prenda de Explora", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WardrobeSuggestionDto> completeWardrobe(CompleteWardrobeRequest request, boolean premium) {
        requirePremium(premium);
        List<WardrobeItem> wardrobe = request.wardrobeOrEmpty();
        Map<String, Long> byCategory = wardrobe.stream()
                .filter(w -> w.category() != null)
                .collect(Collectors.groupingBy(w -> w.category().trim().toLowerCase(Locale.ROOT), Collectors.counting()));
        String style = normalizeStyle(request.primaryStyle());
        List<ExploreItem> catalog = exploreItemRepository.findByActiveTrueOrderByNameAsc();

        List<WardrobeSuggestionDto> suggestions = new ArrayList<>();
        if (count(byCategory, "abrigo") == 0) {
            suggestions.add(suggestion("abrigo", "Una chaqueta o abrigo",
                    "Con una capa encima, lo que ya tienes sirve para más ocasiones y para los días frescos.",
                    catalog, style, item -> true));
        }
        if (count(byCategory, "calzado") < 2) {
            suggestions.add(suggestion("calzado", "Otro par de zapatos",
                    "Con dos pares distintos, el mismo outfit pasa de casual a más arreglado.", catalog, style, item -> true));
        }
        if (count(byCategory, "arriba") < 3) {
            suggestions.add(suggestion("arriba", "Una parte de arriba versátil",
                    "Las partes de arriba son las que más se cambian: una más multiplica tus combinaciones.",
                    catalog, style, item -> true));
        }
        if (count(byCategory, "abajo") < 2) {
            suggestions.add(suggestion("abajo", "Otra parte de abajo",
                    "Un segundo pantalón o falda te deja repetir las partes de arriba sin que se note.",
                    catalog, style, item -> true));
        }
        boolean allNeutral = wardrobe.size() >= 3 && wardrobe.stream()
                .allMatch(w -> w.color() != null && NEUTRALS.contains(w.color().trim().toLowerCase(Locale.ROOT)));
        if (allNeutral) {
            suggestions.add(suggestion("arriba", "Una prenda con color",
                    "Tu armario es casi todo neutro: una prenda de color le da vida sin complicarte.",
                    catalog, style, item -> !NEUTRALS.contains(item.getColor())));
        }
        if (count(byCategory, "accesorio") == 0) {
            suggestions.add(suggestion("accesorio", "Un accesorio sencillo",
                    "Un accesorio cambia un outfit básico sin comprar ropa nueva.", catalog, style, item -> true));
        }
        return suggestions.stream().filter(s -> !s.items().isEmpty()).limit(MAX_SUGGESTIONS).toList();
    }

    private static long count(Map<String, Long> byCategory, String category) {
        return byCategory.getOrDefault(category, 0L);
    }

    /** Prendas del catálogo de esa categoría; primero las del estilo de la persona. */
    private static WardrobeSuggestionDto suggestion(String category, String title, String reason,
                                                    List<ExploreItem> catalog, String style,
                                                    Predicate<ExploreItem> extra) {
        List<ExploreItemDto> items = catalog.stream()
                .filter(i -> i.getCategory().equals(category))
                .filter(extra)
                .sorted(Comparator.comparing((ExploreItem i) -> style != null && i.getStyles().contains(style) ? 0 : 1)
                        .thenComparing(ExploreItem::getName))
                .limit(SUGGESTED_ITEMS)
                .map(ExploreItemDto::from)
                .toList();
        return new WardrobeSuggestionDto(category, title, reason, items);
    }

    /** Acepta el código ("clasico") o el nombre ("Clásico") del estilo. */
    static String normalizeStyle(String style) {
        if (style == null || style.isBlank()) {
            return null;
        }
        String sinTildes = Normalizer.normalize(style.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    private static void requirePremium(boolean premium) {
        if (!premium) {
            throw new PremiumRequiredException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminExploreItemDto> findAll() {
        return exploreItemRepository.findAllByOrderByNameAsc().stream().map(AdminExploreItemDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExploreItemDto findById(Long id) {
        return AdminExploreItemDto.from(load(id));
    }

    @Override
    @Transactional
    public AdminExploreItemDto create(ExploreItemRequest request) {
        ExploreItem item = new ExploreItem();
        apply(item, request);
        return AdminExploreItemDto.from(exploreItemRepository.save(item));
    }

    @Override
    @Transactional
    public AdminExploreItemDto update(Long id, ExploreItemRequest request) {
        ExploreItem item = load(id);
        apply(item, request);
        exploreItemRepository.flush();
        return AdminExploreItemDto.from(item);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        exploreItemRepository.delete(load(id));
    }

    private ExploreItem load(Long id) {
        return exploreItemRepository.findById(id).orElseThrow(() -> new NotFoundException("Prenda de Explora", id));
    }

    private static void apply(ExploreItem item, ExploreItemRequest request) {
        item.setName(request.name().trim());
        item.setImageUrl(request.imageUrl().trim());
        item.setImageCredit(request.imageCredit() == null || request.imageCredit().isBlank() ? null : request.imageCredit().trim());
        item.setCategory(request.category());
        item.setColor(request.color());
        item.setStyles(new HashSet<>(request.styles().stream().map(ExploreServiceImpl::normalizeStyle).toList()));
        item.setWhyItFits(request.whyItFits().trim());
        item.setTrend(request.trend());
        item.setAccessory(request.accessory());
        item.setActive(request.active());
        List<CombineIdea> ideas = new ArrayList<>();
        for (CombineIdeaDto idea : request.combineIdeas() == null ? List.<CombineIdeaDto>of() : request.combineIdeas()) {
            ideas.add(new CombineIdea(idea.context(), idea.items().stream().map(String::trim).toList()));
        }
        item.setCombineIdeas(ideas);
    }
}
