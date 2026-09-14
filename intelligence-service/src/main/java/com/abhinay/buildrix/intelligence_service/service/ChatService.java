package com.abhinay.buildrix.intelligence_service.service;

import com.abhinay.buildrix.intelligence_service.dto.chat.ChatResponse;
import com.abhinay.buildrix.intelligence_service.entity.ChatSession;
import org.springframework.ai.chat.metadata.Usage;

import java.util.List;
import java.util.UUID;

public interface ChatService {
     List<ChatResponse> getProjectChatHistory(UUID projectId);

     ChatSession createChatSessionIfNotExists(UUID projectId, UUID userId);

     void finalizeChats(String userMessage, ChatSession chatSession, String fullText,
                        Long duration, Usage usage);
}
