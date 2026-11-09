package com.orlo.api.dto.admin;

import com.orlo.api.entities.Store;

import java.util.List;

public record StoreDto(Long id, String name, String logoUrl, String garmentTypes, String url, boolean active,
                       boolean affiliate, List<String> categories) {

    public static StoreDto from(Store store) {
        return new StoreDto(store.getId(), store.getName(), store.getLogoUrl(), store.getGarmentTypes(), store.getUrl(),
                store.isActive(), store.isAffiliate(), store.getCategories().stream().sorted().toList());
    }
}
