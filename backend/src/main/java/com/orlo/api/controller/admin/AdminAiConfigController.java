package com.orlo.api.controller.admin;

import com.orlo.api.dto.admin.AiConfigDto;
import com.orlo.api.service.AiConfigService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Instrucciones, modelo y límite del asesor: se cambian sin tocar el código. */
@RestController
@RequestMapping("/api/v1/admin/ai-config")
@Tag(name = "Panel · Asesor IA")
@SecurityRequirement(name = "admin")
public class AdminAiConfigController {

    private final AiConfigService aiConfigService;

    public AdminAiConfigController(AiConfigService aiConfigService) {
        this.aiConfigService = aiConfigService;
    }

    @GetMapping
    public AiConfigDto get() {
        return aiConfigService.get();
    }

    @PutMapping
    public AiConfigDto update(@Valid @RequestBody AiConfigDto request) {
        return aiConfigService.update(request);
    }
}
