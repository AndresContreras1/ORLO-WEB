package com.orlo.api.service.impl;

import com.orlo.api.entities.AiUsage;
import com.orlo.api.exception.LimitReachedException;
import com.orlo.api.repository.AiUsageRepository;
import com.orlo.api.service.UsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class UsageServiceImpl implements UsageService {

    /** El día se cuenta con la hora de Colombia. */
    static final ZoneId ZONE = ZoneId.of("America/Bogota");
    private static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000);

    private final AiUsageRepository aiUsageRepository;
    private final Clock clock;

    @Autowired
    public UsageServiceImpl(AiUsageRepository aiUsageRepository) {
        this(aiUsageRepository, Clock.system(ZONE));
    }

    UsageServiceImpl(AiUsageRepository aiUsageRepository, Clock clock) {
        this.aiUsageRepository = aiUsageRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public int remainingToday(String uid, int dailyLimit) {
        int used = aiUsageRepository.findByUidAndUsageDate(uid, today()).map(AiUsage::getMessages).orElse(0);
        return Math.max(0, dailyLimit - used);
    }

    @Override
    @Transactional
    public int reserve(String uid, int dailyLimit) {
        LocalDate day = today();
        AiUsage usage = aiUsageRepository.findForUpdate(uid, day)
                .orElseGet(() -> aiUsageRepository.save(new AiUsage(uid, day)));
        if (usage.getMessages() >= dailyLimit) {
            throw new LimitReachedException(dailyLimit);
        }
        usage.setMessages(usage.getMessages() + 1);
        return dailyLimit - usage.getMessages();
    }

    @Override
    @Transactional
    public void reserveDailyOutfit(String uid, int dailyLimit) {
        LocalDate day = today();
        AiUsage usage = aiUsageRepository.findForUpdate(uid, day)
                .orElseGet(() -> aiUsageRepository.save(new AiUsage(uid, day)));
        if (usage.getDailyOutfits() >= dailyLimit) {
            throw new LimitReachedException(dailyLimit);
        }
        usage.setDailyOutfits(usage.getDailyOutfits() + 1);
    }

    @Override
    @Transactional
    public void release(String uid) {
        aiUsageRepository.findForUpdate(uid, today())
                .filter(u -> u.getMessages() > 0)
                .ifPresent(u -> u.setMessages(u.getMessages() - 1));
    }

    @Override
    @Transactional
    public void recordTokens(String uid, String model, long inputTokens, long outputTokens) {
        LocalDate day = today();
        AiUsage usage = aiUsageRepository.findForUpdate(uid, day)
                .orElseGet(() -> aiUsageRepository.save(new AiUsage(uid, day)));
        usage.setInputTokens(usage.getInputTokens() + inputTokens);
        usage.setOutputTokens(usage.getOutputTokens() + outputTokens);
        usage.setCostUsd(usage.getCostUsd().add(cost(model, inputTokens, outputTokens)));
    }

    /** Costo en USD con los precios por millón de tokens de cada familia de modelos. */
    static BigDecimal cost(String model, long inputTokens, long outputTokens) {
        double[] price = pricePerMillion(model);
        BigDecimal in = BigDecimal.valueOf(inputTokens).multiply(BigDecimal.valueOf(price[0]));
        BigDecimal out = BigDecimal.valueOf(outputTokens).multiply(BigDecimal.valueOf(price[1]));
        return in.add(out).divide(MILLION, 6, RoundingMode.HALF_UP);
    }

    private static double[] pricePerMillion(String model) {
        String m = model == null ? "" : model.toLowerCase();
        if (m.contains("opus")) {
            return new double[]{4.0, 20.0};
        }
        if (m.contains("sonnet")) {
            return new double[]{2.0, 10.0};
        }
        return new double[]{0.10, 0.50};
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
