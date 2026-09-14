package com.abhinay.buildrix.intelligence_service.service.impl;

import com.abhinay.buildrix.common_lib.dto.PlanResponse;
import com.abhinay.buildrix.common_lib.dto.SubscriptionResponse;
import com.abhinay.buildrix.common_lib.security.AuthUtil;
import com.abhinay.buildrix.intelligence_service.client.AccountServiceClient;
import com.abhinay.buildrix.intelligence_service.dto.usage.PlanLimitsResponse;
import com.abhinay.buildrix.intelligence_service.dto.usage.UsageTodayResponse;
import com.abhinay.buildrix.intelligence_service.entity.UsageLog;
import com.abhinay.buildrix.intelligence_service.repository.UsageLogRepository;
import com.abhinay.buildrix.intelligence_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class UsageServiceImpl implements UsageService {

    private final UsageLogRepository usageLogRepository;
    private final AuthUtil authUtil;
    private final AccountServiceClient accountServiceClient;
    private static Integer FREE_DAILY_TOKEN_LIMIT = 100000;

    @Override
    public UsageTodayResponse getTodayUsage(UUID userId) {
        return null;
    }

    @Override
    public PlanLimitsResponse getUsageLimits(UUID userId) {
        return null;
    }

    @Override
    public void recordTokenUsage(UUID userId, int actualTokens) {
        LocalDate today = LocalDate.now();

        UsageLog todayLog = usageLogRepository.findByUserIdAndDate(userId, today).
                orElseGet(() -> createNewDailyLog(userId, today));

        todayLog.setTokensUsed(todayLog.getTokensUsed() + actualTokens);
        usageLogRepository.save(todayLog);
    }

    @Override
    public void checkDailyTokensUsage() {
        UUID userId = authUtil.getCurrentUserId();
        SubscriptionResponse subscriptionResponse = accountServiceClient.getUserSubscription();
        PlanResponse plan = subscriptionResponse.plan();

        LocalDate today = LocalDate.now();

        UsageLog todayLog = usageLogRepository.findByUserIdAndDate(userId, today).
                orElseGet(() -> createNewDailyLog(userId, today));
        int currentUsage = todayLog.getTokensUsed();
        if(plan == null){
            if(currentUsage >=  FREE_DAILY_TOKEN_LIMIT) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Daily limit reached, Upgrade now");
            }
            return;
        }
        if(plan.unlimitedAi()) return;

        int limit = plan.maxTokensPerDay();

        if(currentUsage >=  limit) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Daily limit reached, Upgrade now");
        }

    }
    private UsageLog createNewDailyLog(UUID userId, LocalDate date) {
        UsageLog newLog = UsageLog.builder()
                .userId(userId)
                .date(date)
                .tokensUsed(0)
                .build();
        return usageLogRepository.save(newLog);
    }

}
