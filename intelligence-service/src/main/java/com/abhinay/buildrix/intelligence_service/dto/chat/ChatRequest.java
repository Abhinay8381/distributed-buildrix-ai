package com.abhinay.buildrix.intelligence_service.dto.chat;

import java.util.UUID;

public record ChatRequest(
        String message,
        UUID projectId
) {
}
