package com.abhinay.buildrix.intelligence_service.service;

import com.abhinay.buildrix.intelligence_service.dto.usage.PlanLimitsResponse;
import com.abhinay.buildrix.intelligence_service.dto.usage.UsageTodayResponse;

import java.util.UUID;

public interface UsageService {
    UsageTodayResponse getTodayUsage(UUID userId);

    PlanLimitsResponse getUsageLimits(UUID userId);

    void recordTokenUsage(UUID userId, int actualTokens);
    void checkDailyTokensUsage();
}
