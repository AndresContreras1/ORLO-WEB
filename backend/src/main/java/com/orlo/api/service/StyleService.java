package com.orlo.api.service;

import com.orlo.api.dto.StyleDto;
import com.orlo.api.dto.admin.StyleRequest;

import java.util.List;

public interface StyleService {

    /** Estilos para la app: la galería es corta si el usuario no es premium. */
    List<StyleDto> listForApp(boolean premium);

    List<StyleDto> findAll();

    StyleDto findById(Long id);

    StyleDto create(StyleRequest request);

    StyleDto update(Long id, StyleRequest request);

    void delete(Long id);
}
