package com.orlo.api.service;

import com.orlo.api.dto.AdvisorMessageRequest;
import com.orlo.api.dto.AdvisorReplyDto;
import com.orlo.api.security.AppPrincipal;

public interface AdvisorService {

    /**
     * Responde una pregunta de vestimenta con el perfil de la persona.
     * Exige premium y descuenta un mensaje del límite diario antes de llamar al modelo.
     */
    AdvisorReplyDto ask(AppPrincipal principal, AdvisorMessageRequest request);
}
