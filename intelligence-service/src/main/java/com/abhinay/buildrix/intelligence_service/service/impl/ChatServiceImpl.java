package com.abhinay.buildrix.intelligence_service.service.impl;

import com.abhinay.buildrix.common_lib.enums.ChatEventType;
import com.abhinay.buildrix.common_lib.enums.MessageRole;
import com.abhinay.buildrix.common_lib.events.FileStoreRequestEvent;
import com.abhinay.buildrix.common_lib.exceptions.ResourceNotFoundException;
import com.abhinay.buildrix.common_lib.security.AuthUtil;
import com.abhinay.buildrix.intelligence_service.dto.chat.ChatResponse;
import com.abhinay.buildrix.intelligence_service.entity.ChatEvent;
import com.abhinay.buildrix.intelligence_service.entity.ChatMessage;
import com.abhinay.buildrix.intelligence_service.entity.ChatSession;
import com.abhinay.buildrix.intelligence_service.entity.ChatSessionId;
import com.abhinay.buildrix.intelligence_service.llm.LLMResponseParser;
import com.abhinay.buildrix.intelligence_service.mapper.ChatMapper;
import com.abhinay.buildrix.intelligence_service.repository.ChatEventRepository;
import com.abhinay.buildrix.intelligence_service.repository.ChatMessageRepository;
import com.abhinay.buildrix.intelligence_service.repository.ChatSessionRepository;
import com.abhinay.buildrix.intelligence_service.service.ChatService;
import com.abhinay.buildrix.intelligence_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    private final ChatEventRepository chatEventRepository;
    private final LLMResponseParser llmResponseParser;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AuthUtil authUtil;
    private final ChatMapper chatMapper;
    private final UsageService usageService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String FILE_STORAGE_REQUEST_EVENT = "file-storage-request-event";

    public List<ChatResponse> getProjectChatHistory(UUID projectId){
        UUID userId = authUtil.getCurrentUserId();
        ChatSession chatSession = chatSessionRepository.findById(new ChatSessionId(userId, projectId))
                .orElseThrow(() -> new ResourceNotFoundException("Chat Session", projectId + "-" +userId));

        List<ChatMessage> messages = chatMessageRepository.findChatMessagesByChatSession(chatSession);
        return chatMapper.fromListOfChatMessage(messages);
    }

    @Transactional
    @Override
    public ChatSession createChatSessionIfNotExists(UUID projectId, UUID userId) {
        ChatSessionId chatSessionId = new ChatSessionId(userId, projectId);
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId)
                .orElse(null);

        if(chatSession == null){
            chatSession = ChatSession.builder()
                    .id(chatSessionId)
                    .build();
            chatSession = chatSessionRepository.save(chatSession);
        }
        return chatSession;
    }

    @Transactional
    @Override
    public void finalizeChats(String userMessage, ChatSession chatSession, String fullText, Long duration, Usage usage) {
        UUID projectId = chatSession.getId().getProjectId();

        if(usage != null)
            usageService.recordTokenUsage(chatSession.getId().getUserId(), usage.getTotalTokens());

        chatMessageRepository.save(
                ChatMessage.builder()
                        .chatSession(chatSession)
                        .role(MessageRole.USER)
                        .content(userMessage)
                        .tokensUsed(usage.getPromptTokens())
                        .build()
        );

        ChatMessage assistantMessage = chatMessageRepository.save(
                ChatMessage.builder()
                        .chatSession(chatSession)
                        .role(MessageRole.ASSISTANT)
                        .content("Assistant message here....")
                        .tokensUsed(usage.getCompletionTokens())
                        .build()
        );
        List<ChatEvent> chatEvents = llmResponseParser.parseChatEvents(fullText, assistantMessage);

        chatEvents.add(ChatEvent.builder()
                .chatMessage(assistantMessage)
                .type(ChatEventType.THOUGHT)
                .sequenceOrder(0)
                .content("Thought for " + duration + "s")
                .build());
        chatEventRepository.saveAll(chatEvents);
        chatEvents.
                stream()
                .filter(chatEvent ->  chatEvent.getType() == ChatEventType.FILE_EDIT)
                .forEach(chatEvent ->

                {
                    UUID sagaId = UUID.randomUUID();
                    chatEvent.setSagaId(sagaId.toString());
                    FileStoreRequestEvent fileStoreRequestEvent =
                            new FileStoreRequestEvent(projectId,
                                    sagaId.toString(),
                                    chatEvent.getFilePath(),
                                    chatEvent.getContent(),
                                    null
                                    );
                    log.info("Storage request event sent: {}", chatEvent.getFilePath());
                    kafkaTemplate.send(FILE_STORAGE_REQUEST_EVENT, "project-"+projectId, fileStoreRequestEvent);
                });


    }
}
