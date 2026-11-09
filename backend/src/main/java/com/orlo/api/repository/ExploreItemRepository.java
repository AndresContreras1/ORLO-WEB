package com.orlo.api.repository;

import com.orlo.api.entities.ExploreItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExploreItemRepository extends JpaRepository<ExploreItem, Long> {

    @EntityGraph(attributePaths = {"styles"})
    List<ExploreItem> findByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = {"styles"})
    List<ExploreItem> findAllByOrderByNameAsc();

    Optional<ExploreItem> findByIdAndActiveTrue(Long id);
}
