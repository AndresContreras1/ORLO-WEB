package com.orlo.api.dto.admin;

/** Token del panel. Nunca devuelve la entidad del administrador. */
public record LoginResponse(String token, String tokenType, long expiresIn, String email) {
}
