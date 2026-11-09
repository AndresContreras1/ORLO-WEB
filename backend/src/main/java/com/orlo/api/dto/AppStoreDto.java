package com.orlo.api.dto;

import com.orlo.api.entities.Store;

import java.util.Arrays;
import java.util.List;

/** Tienda sugerida tal como la ve la app. */
public record AppStoreDto(Long id, String name, String logoUrl, List<String> clothingTypes, String url,
                          boolean affiliate) {

    public static AppStoreDto from(Store store) {
        List<String> types = Arrays.stream(store.getGarmentTypes().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        return new AppStoreDto(store.getId(), store.getName(), store.getLogoUrl(), types, store.getUrl(),
                store.isAffiliate());
    }
}
