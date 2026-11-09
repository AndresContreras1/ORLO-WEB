package com.orlo.api.controller;

import com.orlo.api.dto.QuizDto;
import com.orlo.api.dto.QuizScoreRequest;
import com.orlo.api.dto.QuizScoreResponse;
import com.orlo.api.security.AppPrincipal;
import com.orlo.api.service.AppUserService;
import com.orlo.api.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quiz")
@Tag(name = "App · Cuestionario")
@SecurityRequirement(name = "firebase")
public class QuizController {

    private final QuizService quizService;
    private final AppUserService appUserService;

    public QuizController(QuizService quizService, AppUserService appUserService) {
        this.quizService = quizService;
        this.appUserService = appUserService;
    }

    @GetMapping("/active")
    @Operation(summary = "Cuestionario activo con sus opciones")
    public QuizDto active() {
        return quizService.activeQuiz();
    }

    @PostMapping("/score")
    @Operation(summary = "Calcula el estilo principal y el secundario a partir de las respuestas")
    public QuizScoreResponse score(@Parameter(hidden = true) @AuthenticationPrincipal AppPrincipal principal,
                                   @Valid @RequestBody QuizScoreRequest request) {
        boolean premium = appUserService.registerVisit(principal);
        return quizService.score(principal.uid(), premium, request);
    }
}
