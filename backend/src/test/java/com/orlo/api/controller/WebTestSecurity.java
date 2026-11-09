package com.orlo.api.controller;

import com.orlo.api.security.ApiAccessDeniedHandler;
import com.orlo.api.security.JwtAuthEntryPoint;
import com.orlo.api.security.JwtService;
import com.orlo.api.security.ProblemDetailWriter;
import com.orlo.api.security.SecurityConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Lo que @WebMvcTest no carga solo y la cadena de seguridad necesita. Así las pruebas de
 * controladores corren con las mismas reglas (401/403) que la aplicación.
 */
@TestConfiguration
@Import({SecurityConfig.class, JwtService.class, JwtAuthEntryPoint.class, ApiAccessDeniedHandler.class,
        ProblemDetailWriter.class})
public class WebTestSecurity {
}
