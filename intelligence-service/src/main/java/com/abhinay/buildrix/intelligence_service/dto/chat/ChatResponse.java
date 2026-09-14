package com.abhinay.buildrix.intelligence_service.dto.chat;

import com.abhinay.buildrix.common_lib.enums.MessageRole;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatResponse(
        UUID id,
        MessageRole role,
        List<ChatEventResponse> events,
        String content,
        Integer tokensUsed,
        Instant createdAt

) {
}
