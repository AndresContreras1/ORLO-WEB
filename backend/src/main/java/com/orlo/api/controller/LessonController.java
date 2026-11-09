package com.orlo.api.controller;

import com.orlo.api.dto.LessonDetailDto;
import com.orlo.api.dto.LessonSummaryDto;
import com.orlo.api.security.AppPrincipal;
import com.orlo.api.service.AppUserService;
import com.orlo.api.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lessons")
@Tag(name = "App · Aprende")
@SecurityRequirement(name = "firebase")
public class LessonController {

    private final LessonService lessonService;
    private final AppUserService appUserService;

    public LessonController(LessonService lessonService, AppUserService appUserService) {
        this.lessonService = lessonService;
        this.appUserService = appUserService;
    }

    @GetMapping
    @Operation(summary = "Lista de cápsulas publicadas")
    public List<LessonSummaryDto> list() {
        return lessonService.listPublished();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Cápsula completa. 403 PREMIUM_REQUIRED si no es la muestra gratis y el usuario no es premium")
    public LessonDetailDto detail(@Parameter(hidden = true) @AuthenticationPrincipal AppPrincipal principal,
                                  @PathVariable Long id) {
        return lessonService.detail(id, appUserService.registerVisit(principal));
    }
}
