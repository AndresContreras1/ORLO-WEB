package com.orlo.api.service.impl;

import com.orlo.api.dto.admin.MetricsDto;
import com.orlo.api.repository.AiUsageRepository;
import com.orlo.api.repository.AppUserRepository;
import com.orlo.api.repository.LessonRepository;
import com.orlo.api.repository.QuizResultRepository;
import com.orlo.api.repository.StoreRepository;
import com.orlo.api.service.MetricsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class MetricsServiceImpl implements MetricsService {

    private final AppUserRepository appUserRepository;
    private final QuizResultRepository quizResultRepository;
    private final AiUsageRepository aiUsageRepository;
    private final LessonRepository lessonRepository;
    private final StoreRepository storeRepository;

    public MetricsServiceImpl(AppUserRepository appUserRepository, QuizResultRepository quizResultRepository,
                              AiUsageRepository aiUsageRepository, LessonRepository lessonRepository,
                              StoreRepository storeRepository) {
        this.appUserRepository = appUserRepository;
        this.quizResultRepository = quizResultRepository;
        this.aiUsageRepository = aiUsageRepository;
        this.lessonRepository = lessonRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MetricsDto current() {
        long totalUsers = appUserRepository.count();
        long premiumUsers = appUserRepository.countByPremiumTrue();
        double conversion = totalUsers == 0 ? 0.0
                : BigDecimal.valueOf(premiumUsers * 100.0 / totalUsers).setScale(1, RoundingMode.HALF_UP).doubleValue();
        BigDecimal cost = aiUsageRepository.sumCost();
        return new MetricsDto(totalUsers, premiumUsers, conversion, quizResultRepository.count(),
                aiUsageRepository.sumMessages(), aiUsageRepository.sumMessagesOn(LocalDate.now(UsageServiceImpl.ZONE)),
                cost == null ? BigDecimal.ZERO : cost.setScale(4, RoundingMode.HALF_UP),
                lessonRepository.findByPublishedTrueOrderByPositionAsc().size(),
                storeRepository.findAll().stream().filter(s -> s.isActive()).count());
    }
}
