package com.orlo.api.controller;

import com.orlo.api.dto.AdvisorMessageRequest;
import com.orlo.api.dto.AdvisorReplyDto;
import com.orlo.api.dto.DailyOutfitDto;
import com.orlo.api.dto.DailyOutfitRequest;
import com.orlo.api.security.AppPrincipal;
import com.orlo.api.service.AdvisorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/advisor")
@Tag(name = "App · Asesor IA")
@SecurityRequirement(name = "firebase")
public class AdvisorController {

    private final AdvisorService advisorService;

    public AdvisorController(AdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @PostMapping("/messages")
    @Operation(summary = "Pregunta al asesor. 403 PREMIUM_REQUIRED, 429 LIMIT_REACHED, 503 AI_UNAVAILABLE")
    public AdvisorReplyDto ask(@Parameter(hidden = true) @AuthenticationPrincipal AppPrincipal principal,
                               @Valid @RequestBody AdvisorMessageRequest request) {
        return advisorService.ask(principal, request);
    }

    @PostMapping("/daily-outfit")
    @Operation(summary = "Outfit del día con las prendas del armario. No descuenta mensajes; 429 LIMIT_REACHED al pasar su tope diario")
    public DailyOutfitDto dailyOutfit(@Parameter(hidden = true) @AuthenticationPrincipal AppPrincipal principal,
                                      @Valid @RequestBody DailyOutfitRequest request) {
        return advisorService.dailyOutfit(principal, request);
    }
}
