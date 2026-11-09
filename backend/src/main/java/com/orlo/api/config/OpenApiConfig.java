package com.orlo.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "ORLO API", version = "v1",
        description = "Contenido, cuestionario de estilo, asesor con IA y panel de administración"))
@SecurityScheme(name = "firebase", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
        description = "ID token de Firebase Authentication (app móvil)")
@SecurityScheme(name = "admin", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
        description = "Token del panel: POST /api/v1/admin/auth/login")
public class OpenApiConfig {
}
