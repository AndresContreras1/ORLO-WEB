package com.orlo.api.controller;

import com.orlo.api.dto.AppStoreDto;
import com.orlo.api.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@Tag(name = "App · Tiendas sugeridas")
@SecurityRequirement(name = "firebase")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    @Operation(summary = "Tiendas activas; con category, las que venden esa categoría (si ninguna, todas)")
    public List<AppStoreDto> list(@RequestParam(required = false) String category) {
        return storeService.listForApp(category);
    }
}
