package com.orlo.api.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/** Tienda sugerida del catálogo manual. */
@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "garment_types", nullable = false, length = 200)
    private String garmentTypes;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean affiliate;

    /** Categorías de prenda que vende (códigos de CatalogCodes): sirven para "Dónde conseguirla". */
    @ElementCollection
    @CollectionTable(name = "store_categories", joinColumns = @JoinColumn(name = "store_id"))
    @Column(name = "category", nullable = false, length = 20)
    private Set<String> categories = new HashSet<>();

    public Store(String name, String logoUrl, String garmentTypes, String url) {
        this.name = name;
        this.logoUrl = logoUrl;
        this.garmentTypes = garmentTypes;
        this.url = url;
    }
}
