package com.orlo.api.controller;

import com.orlo.api.dto.StyleDto;
import com.orlo.api.security.AppPrincipal;
import com.orlo.api.service.AppUserService;
import com.orlo.api.service.StyleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/styles")
@Tag(name = "App · Estilos")
@SecurityRequirement(name = "firebase")
public class StyleController {

    private final StyleService styleService;
    private final AppUserService appUserService;

    public StyleController(StyleService styleService, AppUserService appUserService) {
        this.styleService = styleService;
        this.appUserService = appUserService;
    }

    @GetMapping
    @Operation(summary = "Estilos base con paleta y galería (corta si no es premium)")
    public List<StyleDto> list(@Parameter(hidden = true) @AuthenticationPrincipal AppPrincipal principal) {
        return styleService.listForApp(appUserService.registerVisit(principal));
    }
}
