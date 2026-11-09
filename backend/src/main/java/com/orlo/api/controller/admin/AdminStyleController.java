package com.orlo.api.controller.admin;

import com.orlo.api.dto.StyleDto;
import com.orlo.api.dto.admin.StyleRequest;
import com.orlo.api.service.StyleService;
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
@RequestMapping("/api/v1/admin/styles")
@Tag(name = "Panel · Estilos")
@SecurityRequirement(name = "admin")
public class AdminStyleController {

    private final StyleService styleService;

    public AdminStyleController(StyleService styleService) {
        this.styleService = styleService;
    }

    @GetMapping
    public List<StyleDto> findAll() {
        return styleService.findAll();
    }

    @GetMapping("/{id}")
    public StyleDto findById(@PathVariable Long id) {
        return styleService.findById(id);
    }

    @PostMapping
    public ResponseEntity<StyleDto> create(@Valid @RequestBody StyleRequest request) {
        StyleDto created = styleService.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public StyleDto update(@PathVariable Long id, @Valid @RequestBody StyleRequest request) {
        return styleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        styleService.delete(id);
    }
}
