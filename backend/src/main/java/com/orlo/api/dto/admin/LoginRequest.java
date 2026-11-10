package com.orlo.api.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** El tope de la contraseña evita gastar CPU hasheando textos enormes (BCrypt solo usa 72 bytes). */
public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido")
        @Size(max = 254, message = "El correo es muy largo") String email,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 128, message = "La contraseña es muy larga") String password) {
}
