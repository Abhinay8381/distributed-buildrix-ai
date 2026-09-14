package com.abhinay.buildrix.intelligence_service.service;

import com.abhinay.buildrix.intelligence_service.dto.chat.ChatStreamResponse;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface AiGenerationService {
    Flux<ChatStreamResponse> streamResponse(String message, UUID projectId);
}
