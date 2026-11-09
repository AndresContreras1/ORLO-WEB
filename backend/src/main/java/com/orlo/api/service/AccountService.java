package com.orlo.api.service;

import com.orlo.api.dto.MeDto;
import com.orlo.api.security.AppPrincipal;

/** Lo que la app consulta de su propia cuenta: premium, uso del asesor y borrado. */
public interface AccountService {

    MeDto me(AppPrincipal principal);

    void deleteAccount(AppPrincipal principal);
}
