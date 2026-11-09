package com.orlo.api.service;

import com.orlo.api.dto.admin.StoreDto;
import com.orlo.api.dto.admin.StoreRequest;

import java.util.List;

public interface StoreService {

    List<StoreDto> findAll();

    StoreDto findById(Long id);

    StoreDto create(StoreRequest request);

    StoreDto update(Long id, StoreRequest request);

    void delete(Long id);
}
