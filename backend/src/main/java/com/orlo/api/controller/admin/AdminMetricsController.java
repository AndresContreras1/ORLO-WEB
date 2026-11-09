package com.orlo.api.controller.admin;

import com.orlo.api.dto.admin.MetricsDto;
import com.orlo.api.service.MetricsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/metrics")
@Tag(name = "Panel · Métricas")
@SecurityRequirement(name = "admin")
public class AdminMetricsController {

    private final MetricsService metricsService;

    public AdminMetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping
    public MetricsDto current() {
        return metricsService.current();
    }
}
