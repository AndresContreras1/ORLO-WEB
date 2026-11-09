package com.orlo.api.service.impl;

import com.orlo.api.dto.PageResponse;
import com.orlo.api.dto.admin.AdminAppUserDto;
import com.orlo.api.entities.AppUser;
import com.orlo.api.exception.NotFoundException;
import com.orlo.api.repository.AiUsageRepository;
import com.orlo.api.repository.AppUserRepository;
import com.orlo.api.repository.QuizResultRepository;
import com.orlo.api.security.AppPrincipal;
import com.orlo.api.service.AppUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final AiUsageRepository aiUsageRepository;
    private final QuizResultRepository quizResultRepository;
    /** Con ORLO_PREMIUM_PARA_TODOS=true todos se tratan como premium (pruebas). El panel sigue viendo el valor real. */
    private final boolean premiumForEveryone;

    public AppUserServiceImpl(AppUserRepository appUserRepository, AiUsageRepository aiUsageRepository,
                              QuizResultRepository quizResultRepository,
                              @Value("${app.premium.for-everyone:false}") boolean premiumForEveryone) {
        this.appUserRepository = appUserRepository;
        this.aiUsageRepository = aiUsageRepository;
        this.quizResultRepository = quizResultRepository;
        this.premiumForEveryone = premiumForEveryone;
    }

    @Override
    @Transactional
    public boolean registerVisit(AppPrincipal principal) {
        AppUser user = appUserRepository.findById(principal.uid())
                .orElseGet(() -> appUserRepository.save(new AppUser(principal.uid(), principal.email())));
        user.setLastSeenAt(Instant.now());
        if (principal.email() != null) {
            user.setEmail(principal.email());
        }
        return premiumForEveryone || user.isPremium();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPremium(String uid) {
        return premiumForEveryone || appUserRepository.findById(uid).map(AppUser::isPremium).orElse(false);
    }

    @Override
    @Transactional
    public void deleteLocalData(String uid) {
        aiUsageRepository.deleteByUid(uid);
        quizResultRepository.deleteByUid(uid);
        appUserRepository.deleteById(uid);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminAppUserDto> list(String email, Pageable pageable) {
        var page = email == null || email.isBlank()
                ? appUserRepository.findAll(pageable)
                : appUserRepository.findByEmailContainingIgnoreCase(email.trim(), pageable);
        return PageResponse.of(page, AdminAppUserDto::from);
    }

    @Override
    @Transactional
    public AdminAppUserDto setPremium(String uid, boolean premium) {
        AppUser user = appUserRepository.findById(uid).orElseThrow(() -> new NotFoundException("Usuario", uid));
        user.setPremium(premium);
        return AdminAppUserDto.from(user);
    }
}
