package com.orlo.api.service.impl;

import com.orlo.api.dto.admin.AiConfigDto;
import com.orlo.api.entities.AiConfig;
import com.orlo.api.exception.NotFoundException;
import com.orlo.api.repository.AiConfigRepository;
import com.orlo.api.service.AiConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiConfigServiceImpl implements AiConfigService {

    private final AiConfigRepository aiConfigRepository;

    public AiConfigServiceImpl(AiConfigRepository aiConfigRepository) {
        this.aiConfigRepository = aiConfigRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AiConfigDto get() {
        return AiConfigDto.from(load());
    }

    @Override
    @Transactional
    public AiConfigDto update(AiConfigDto request) {
        AiConfig config = load();
        config.setSystemPrompt(request.systemPrompt().trim());
        config.setModel(request.model().trim());
        config.setDailyLimit(request.dailyLimit());
        config.setMaxOutputTokens(request.maxOutputTokens());
        return AiConfigDto.from(config);
    }

    @Override
    @Transactional(readOnly = true)
    public int dailyLimit() {
        return load().getDailyLimit();
    }

    private AiConfig load() {
        return aiConfigRepository.findById(AiConfig.SINGLETON_ID)
                .orElseThrow(() -> new NotFoundException("Configuración de IA", AiConfig.SINGLETON_ID));
    }
}
