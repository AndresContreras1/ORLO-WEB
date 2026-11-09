package com.orlo.api.service.impl;

import com.orlo.api.dto.StyleDto;
import com.orlo.api.dto.admin.StyleRequest;
import com.orlo.api.entities.Style;
import com.orlo.api.exception.BusinessRuleException;
import com.orlo.api.exception.NotFoundException;
import com.orlo.api.repository.OptionStyleScoreRepository;
import com.orlo.api.repository.StyleRepository;
import com.orlo.api.service.StyleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class StyleServiceImpl implements StyleService {

    private final StyleRepository styleRepository;
    private final OptionStyleScoreRepository scoreRepository;
    private final int freeGallerySize;

    public StyleServiceImpl(StyleRepository styleRepository, OptionStyleScoreRepository scoreRepository,
                            @Value("${app.premium.free-gallery-size}") int freeGallerySize) {
        this.styleRepository = styleRepository;
        this.scoreRepository = scoreRepository;
        this.freeGallerySize = freeGallerySize;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StyleDto> listForApp(boolean premium) {
        int limit = premium ? Integer.MAX_VALUE : freeGallerySize;
        return styleRepository.findAllByOrderByNameAsc().stream().map(s -> StyleDto.from(s, limit)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StyleDto> findAll() {
        return styleRepository.findAllByOrderByNameAsc().stream().map(StyleDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StyleDto findById(Long id) {
        return StyleDto.from(load(id));
    }

    @Override
    @Transactional
    public StyleDto create(StyleRequest request) {
        if (styleRepository.existsByCode(request.code())) {
            throw new BusinessRuleException("Ya existe un estilo con el código " + request.code());
        }
        Style style = new Style();
        apply(style, request);
        return StyleDto.from(styleRepository.save(style));
    }

    @Override
    @Transactional
    public StyleDto update(Long id, StyleRequest request) {
        Style style = load(id);
        if (styleRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new BusinessRuleException("Ya existe un estilo con el código " + request.code());
        }
        apply(style, request);
        return StyleDto.from(style);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Style style = load(id);
        if (scoreRepository.existsByStyleId(id)) {
            throw new BusinessRuleException("Este estilo tiene puntos en el cuestionario. Quítalos antes de borrarlo");
        }
        styleRepository.delete(style);
    }

    private Style load(Long id) {
        return styleRepository.findById(id).orElseThrow(() -> new NotFoundException("Estilo", id));
    }

    private static void apply(Style style, StyleRequest request) {
        style.setCode(request.code().trim());
        style.setName(request.name().trim());
        style.setDescription(request.description().trim());
        style.setPalette(new ArrayList<>(request.palette().stream().map(String::toUpperCase).toList()));
        style.setGallery(new ArrayList<>(request.gallery() == null ? List.of() : request.gallery()));
    }
}
