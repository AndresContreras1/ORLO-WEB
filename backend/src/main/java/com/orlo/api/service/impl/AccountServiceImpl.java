package com.orlo.api.service.impl;

import com.orlo.api.client.FirebaseAuthClient;
import com.orlo.api.dto.MeDto;
import com.orlo.api.security.AppPrincipal;
import com.orlo.api.service.AccountService;
import com.orlo.api.service.AiConfigService;
import com.orlo.api.service.AppUserService;
import com.orlo.api.service.UsageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AppUserService appUserService;
    private final UsageService usageService;
    private final AiConfigService aiConfigService;
    private final FirebaseAuthClient firebaseAuthClient;

    public AccountServiceImpl(AppUserService appUserService, UsageService usageService,
                              AiConfigService aiConfigService, FirebaseAuthClient firebaseAuthClient) {
        this.appUserService = appUserService;
        this.usageService = usageService;
        this.aiConfigService = aiConfigService;
        this.firebaseAuthClient = firebaseAuthClient;
    }

    @Override
    public MeDto me(AppPrincipal principal) {
        boolean premium = appUserService.registerVisit(principal);
        int dailyLimit = aiConfigService.dailyLimit();
        int remaining = premium ? usageService.remainingToday(principal.uid(), dailyLimit) : 0;
        return new MeDto(principal.uid(), premium, dailyLimit, remaining);
    }

    /**
     * Primero se borra la cuenta de Firebase (si falla, no se toca nada más) y después los datos
     * del backend. Los datos de Firestore y Storage los borra la app antes de llamar aquí.
     */
    @Override
    public void deleteAccount(AppPrincipal principal) {
        firebaseAuthClient.deleteUser(principal.uid());
        appUserService.deleteLocalData(principal.uid());
        log.info("Cuenta {} eliminada", principal.uid());
    }
}
