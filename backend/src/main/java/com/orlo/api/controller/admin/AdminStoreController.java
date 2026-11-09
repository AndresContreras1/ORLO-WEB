package com.orlo.api.controller.admin;

import com.orlo.api.dto.admin.StoreDto;
import com.orlo.api.dto.admin.StoreRequest;
import com.orlo.api.service.StoreService;
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

@RestController
@RequestMapping("/api/v1/admin/stores")
@Tag(name = "Panel · Tiendas")
@SecurityRequirement(name = "admin")
public class AdminStoreController {

    private final StoreService storeService;

    public AdminStoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    public List<StoreDto> findAll() {
        return storeService.findAll();
    }

    @GetMapping("/{id}")
    public StoreDto findById(@PathVariable Long id) {
        return storeService.findById(id);
    }

    @PostMapping
    public ResponseEntity<StoreDto> create(@Valid @RequestBody StoreRequest request) {
        StoreDto created = storeService.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public StoreDto update(@PathVariable Long id, @Valid @RequestBody StoreRequest request) {
        return storeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        storeService.delete(id);
    }
}
