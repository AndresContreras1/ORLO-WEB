package com.orlo.api.controller;

import com.orlo.api.dto.DeleteAccountDto;
import com.orlo.api.dto.MeDto;
import com.orlo.api.security.AppPrincipal;
import com.orlo.api.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** "Mis datos": la identidad sale del token, nunca de un parámetro. */
@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "App · Mi cuenta")
@SecurityRequirement(name = "firebase")
public class MeController {

    private final AccountService accountService;

    public MeController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    @Operation(summary = "Premium y mensajes del asesor que quedan hoy. Registra al usuario la primera vez")
    public MeDto me(@Parameter(hidden = true) @AuthenticationPrincipal AppPrincipal principal) {
        return accountService.me(principal);
    }

    /** Responde 200 con JSON (no 204 vacío): Retrofit en la app falla al leer una respuesta sin cuerpo. */
    @DeleteMapping
    @Operation(summary = "Elimina la cuenta de Firebase y los datos del backend")
    public DeleteAccountDto delete(@Parameter(hidden = true) @AuthenticationPrincipal AppPrincipal principal) {
        accountService.deleteAccount(principal);
        return new DeleteAccountDto(true);
    }
}
