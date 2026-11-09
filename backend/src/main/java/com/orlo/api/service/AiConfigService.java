package com.orlo.api.service;

import com.orlo.api.dto.admin.AiConfigDto;

public interface AiConfigService {

    AiConfigDto get();

    AiConfigDto update(AiConfigDto request);

    int dailyLimit();
}
