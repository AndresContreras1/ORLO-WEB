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
import java.util.List;

/** Estilo base del diagnóstico (casual, urbano, clásico…). */
@Entity
@Table(name = "styles")
@Getter
@Setter
@NoArgsConstructor
public class Style {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "style_palette", joinColumns = @JoinColumn(name = "style_id"))
    @OrderColumn(name = "position")
    @Column(name = "hex", nullable = false, length = 9)
    private List<String> palette = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "style_gallery", joinColumns = @JoinColumn(name = "style_id"))
    @OrderColumn(name = "position")
    @Column(name = "image_url", nullable = false, length = 500)
    private List<String> gallery = new ArrayList<>();

    public Style(String code, String name, String description, List<String> palette, List<String> gallery) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.palette = new ArrayList<>(palette);
        this.gallery = new ArrayList<>(gallery);
    }
}
