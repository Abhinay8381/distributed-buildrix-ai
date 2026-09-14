package com.abhinay.buildrix.intelligence_service.dto.chat;

import com.abhinay.buildrix.common_lib.enums.ChatEventType;

import java.util.UUID;

public record ChatEventResponse(
        UUID id,
        ChatEventType type,
        Integer sequenceOrder,
        String content,
        String filePath,
        String metadata
) {
}
