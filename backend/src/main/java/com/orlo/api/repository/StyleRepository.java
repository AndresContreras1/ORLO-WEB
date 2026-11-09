package com.orlo.api.repository;

import com.orlo.api.entities.Style;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StyleRepository extends JpaRepository<Style, Long> {

    @EntityGraph(attributePaths = {"palette"})
    List<Style> findAllByOrderByNameAsc();

    Optional<Style> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
