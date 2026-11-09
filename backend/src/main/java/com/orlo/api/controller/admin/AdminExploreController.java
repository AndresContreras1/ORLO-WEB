package com.orlo.api.controller.admin;

import com.orlo.api.dto.admin.AdminExploreItemDto;
import com.orlo.api.dto.admin.ExploreItemRequest;
import com.orlo.api.service.ExploreService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** Catálogo de prendas de referencia de Explora. */
@RestController
@RequestMapping("/api/v1/admin/explore-items")
@Tag(name = "Panel · Explora")
@SecurityRequirement(name = "admin")
public class AdminExploreController {

    private final ExploreService exploreService;

    public AdminExploreController(ExploreService exploreService) {
        this.exploreService = exploreService;
    }

    @GetMapping
    public List<AdminExploreItemDto> findAll() {
        return exploreService.findAll();
    }

    @GetMapping("/{id}")
    public AdminExploreItemDto findById(@PathVariable Long id) {
        return exploreService.findById(id);
    }

    @PostMapping
    public ResponseEntity<AdminExploreItemDto> create(@Valid @RequestBody ExploreItemRequest request) {
        AdminExploreItemDto created = exploreService.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public AdminExploreItemDto update(@PathVariable Long id, @Valid @RequestBody ExploreItemRequest request) {
        return exploreService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        exploreService.delete(id);
    }
}
