package com.orlo.api.controller;

import com.orlo.api.dto.CompleteWardrobeRequest;
import com.orlo.api.dto.ExploreItemDetailDto;
import com.orlo.api.dto.ExploreItemDto;
import com.orlo.api.dto.ExploreSection;
import com.orlo.api.dto.WardrobeSuggestionDto;
import com.orlo.api.security.AppPrincipal;
import com.orlo.api.service.AppUserService;
import com.orlo.api.service.ExploreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/explore")
@Tag(name = "App · Explora")
@SecurityRequirement(name = "firebase")
public class ExploreController {

    private final ExploreService exploreService;
    private final AppUserService appUserService;

    public ExploreController(ExploreService exploreService, AppUserService appUserService) {
        this.exploreService = exploreService;
        this.appUserService = appUserService;
    }

    @GetMapping("/items")
    @Operation(summary = "Prendas de referencia de una pestaña; con style, primero las de ese estilo")
    public List<ExploreItemDto> items(@Parameter(hidden = true) @AuthenticationPrincipal AppPrincipal principal,
                                      @RequestParam ExploreSection section,
                                      @RequestParam(required = false) String style) {
        return exploreService.items(section, style, appUserService.registerVisit(principal));
    }

    @GetMapping("/items/{id}")
    @Operation(summary = "Detalle de una prenda con ideas para combinarla (casual, universidad, elegante)")
    public ExploreItemDetailDto item(@Parameter(hidden = true) @AuthenticationPrincipal AppPrincipal principal,
                                     @PathVariable Long id) {
        return exploreService.item(id, appUserService.registerVisit(principal));
    }

    @PostMapping("/complete-wardrobe")
    @Operation(summary = "Tipos de prenda que darían más combinaciones con lo que ya tiene")
    public List<WardrobeSuggestionDto> completeWardrobe(
            @Parameter(hidden = true) @AuthenticationPrincipal AppPrincipal principal,
            @Valid @RequestBody CompleteWardrobeRequest request) {
        return exploreService.completeWardrobe(request, appUserService.registerVisit(principal));
    }
}
