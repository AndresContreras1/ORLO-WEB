package com.orlo.api.service.impl;

import com.orlo.api.dto.AppStoreDto;
import com.orlo.api.dto.admin.StoreDto;
import com.orlo.api.dto.admin.StoreRequest;
import com.orlo.api.entities.Store;
import com.orlo.api.exception.NotFoundException;
import com.orlo.api.repository.StoreRepository;
import com.orlo.api.service.StoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    public StoreServiceImpl(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppStoreDto> listForApp(String category) {
        List<Store> active = storeRepository.findByActiveTrueOrderByNameAsc();
        if (category == null || category.isBlank()) {
            return active.stream().map(AppStoreDto::from).toList();
        }
        List<Store> selling = active.stream().filter(s -> s.getCategories().contains(category.trim())).toList();
        return (selling.isEmpty() ? active : selling).stream().map(AppStoreDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreDto> findAll() {
        return storeRepository.findAllByOrderByNameAsc().stream().map(StoreDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StoreDto findById(Long id) {
        return StoreDto.from(load(id));
    }

    @Override
    @Transactional
    public StoreDto create(StoreRequest request) {
        Store store = new Store();
        apply(store, request);
        return StoreDto.from(storeRepository.save(store));
    }

    @Override
    @Transactional
    public StoreDto update(Long id, StoreRequest request) {
        Store store = load(id);
        apply(store, request);
        return StoreDto.from(store);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        storeRepository.delete(load(id));
    }

    private Store load(Long id) {
        return storeRepository.findById(id).orElseThrow(() -> new NotFoundException("Tienda", id));
    }

    private static void apply(Store store, StoreRequest request) {
        store.setName(request.name().trim());
        store.setLogoUrl(request.logoUrl() == null || request.logoUrl().isBlank() ? null : request.logoUrl().trim());
        store.setGarmentTypes(request.garmentTypes().trim());
        store.setUrl(request.url().trim());
        store.setActive(request.active());
        store.setAffiliate(request.affiliate());
        store.setCategories(new HashSet<>(request.categoriesOrEmpty()));
    }
}
