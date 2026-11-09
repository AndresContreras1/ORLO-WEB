package com.orlo.api.service;

import com.orlo.api.dto.PageResponse;
import com.orlo.api.dto.admin.AdminAppUserDto;
import com.orlo.api.security.AppPrincipal;
import org.springframework.data.domain.Pageable;

public interface AppUserService {

    /** Registra al usuario la primera vez que llega y actualiza su última visita. Devuelve si es premium. */
    boolean registerVisit(AppPrincipal principal);

    boolean isPremium(String uid);

    /** Borra los datos del usuario guardados en el backend. */
    void deleteLocalData(String uid);

    PageResponse<AdminAppUserDto> list(String email, Pageable pageable);

    AdminAppUserDto setPremium(String uid, boolean premium);
}
