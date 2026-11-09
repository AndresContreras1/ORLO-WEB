package com.orlo.api.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Perfil de estilo que la app envía al asesor. Solo lleva lo necesario para recomendar:
 * nada de correo ni apellidos.
 */
public record AdvisorProfile(
        @Size(max = 60) String name,
        @Size(max = 40) String ageRange,
        @Size(max = 20) String garmentPronoun,
        @Size(max = 60) String primaryStyle,
        @Size(max = 60) String secondaryStyle,
        @Size(max = 40) String skinTone,
        @Size(max = 40) String hairColor,
        @Size(max = 40) String eyeColor,
        @Size(max = 60) String climate,
        @Size(max = 60) String budget,
        @Size(max = 10) List<@Size(max = 40) String> occasions) {
}
