package com.orlo.api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

/** Idea para combinar una prenda en un contexto (casual, universidad, elegante). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class CombineIdea {

    private static final String SEPARATOR = "|";

    @Column(nullable = false, length = 40)
    private String context;

    /** Prendas de la idea separadas por "|". */
    @Column(nullable = false, length = 500)
    private String items;

    public CombineIdea(String context, List<String> items) {
        this.context = context;
        this.items = String.join(SEPARATOR, items);
    }

    public List<String> itemList() {
        return Arrays.stream(items.split("\\|")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
