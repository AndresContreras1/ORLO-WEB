package com.orlo.api.controller.admin;

import com.orlo.api.dto.admin.LoginRequest;
import com.orlo.api.dto.admin.LoginResponse;
import com.orlo.api.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/auth")
@Tag(name = "Panel · Autenticación")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * Detrás del proxy del hosting, getRemoteAddr ya es la IP del cliente
     * (server.forward-headers-strategy=framework lee X-Forwarded-For).
     */
    @PostMapping("/login")
    @Operation(summary = "Inicia sesión en el panel y devuelve un JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return adminAuthService.login(request, httpRequest.getRemoteAddr());
    }
}
