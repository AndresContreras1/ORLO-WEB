package com.orlo.api.repository;

import com.orlo.api.entities.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findAllByOrderByNameAsc();
}
