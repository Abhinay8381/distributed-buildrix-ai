package com.abhinay.buildrix.common_lib.dto;
import com.abhinay.buildrix.common_lib.enums.SubscriptionStatus;

import java.time.Instant;

public record SubscriptionResponse(
        PlanResponse plan,
        SubscriptionStatus status,
        Instant currentPeriodEnd,
        Long tokensUsedThisCycle
) {
}
