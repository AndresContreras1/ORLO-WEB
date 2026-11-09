package com.orlo.api.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Prenda de referencia del catálogo de Explora (no es del usuario: es inspiración). */
@Entity
@Table(name = "explore_items")
@Getter
@Setter
@NoArgsConstructor
public class ExploreItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    /** Autor y licencia de la foto, cuando la licencia pide atribución. */
    @Column(name = "image_credit", length = 200)
    private String imageCredit;

    @Column(nullable = false, length = 20)
    private String category;

    @Column(nullable = false, length = 20)
    private String color;

    @Column(name = "why_it_fits", nullable = false, length = 500)
    private String whyItFits;

    @Column(nullable = false)
    private boolean trend;

    @Column(nullable = false)
    private boolean accessory;

    @Column(nullable = false)
    private boolean active = true;

    @ElementCollection
    @CollectionTable(name = "explore_item_styles", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "style_code", nullable = false, length = 40)
    private Set<String> styles = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "explore_combine_ideas", joinColumns = @JoinColumn(name = "item_id"))
    @OrderColumn(name = "position")
    private List<CombineIdea> combineIdeas = new ArrayList<>();
}
