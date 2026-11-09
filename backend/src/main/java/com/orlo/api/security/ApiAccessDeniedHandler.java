package com.orlo.api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** 403: hay sesión, pero sin el permiso que pide la ruta. */
@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ProblemDetailWriter writer;

    public ApiAccessDeniedHandler(ProblemDetailWriter writer) {
        this.writer = writer;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        writer.write(response, HttpStatus.FORBIDDEN, "FORBIDDEN", "No tienes permiso para esta acción");
    }
}
