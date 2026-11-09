package com.orlo.api.controller.admin;

import com.orlo.api.dto.PageResponse;
import com.orlo.api.dto.admin.AdminAppUserDto;
import com.orlo.api.dto.admin.PremiumRequest;
import com.orlo.api.service.AppUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Usuarios de la app y su estado premium (activación manual mientras no hay cobro en la app). */
@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Panel · Usuarios")
@SecurityRequirement(name = "admin")
public class AdminUserController {

    private final AppUserService appUserService;

    public AdminUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    /** El orden es fijo (más recientes primero, y el uid para desempatar): no se ordena por campos del cliente. */
    @GetMapping
    public PageResponse<AdminAppUserDto> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              @RequestParam(required = false) String email) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("uid")));
        return appUserService.list(email, pageable);
    }

    @PutMapping("/{uid}/premium")
    public AdminAppUserDto setPremium(@PathVariable String uid, @Valid @RequestBody PremiumRequest request) {
        return appUserService.setPremium(uid, request.premium());
    }
}
