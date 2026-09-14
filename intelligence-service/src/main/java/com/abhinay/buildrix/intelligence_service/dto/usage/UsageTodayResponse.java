package com.abhinay.buildrix.intelligence_service.dto.usage;

public record UsageTodayResponse(
        Integer tokensUsed,
        Integer tokensRemaining,
        Integer previewsRunning,
        Integer previewsLimit
) {
}
