package com.abhinay.buildrix.intelligence_service.service.impl;

import com.abhinay.buildrix.common_lib.security.AuthUtil;
import com.abhinay.buildrix.intelligence_service.client.WorkspaceServiceClient;
import com.abhinay.buildrix.intelligence_service.dto.chat.ChatStreamResponse;
import com.abhinay.buildrix.intelligence_service.entity.ChatSession;
import com.abhinay.buildrix.intelligence_service.llm.PromptUtils;
import com.abhinay.buildrix.intelligence_service.llm.advisors.FileTreeAdvisor;
import com.abhinay.buildrix.intelligence_service.llm.tools.CodeGenerationTool;
import com.abhinay.buildrix.intelligence_service.service.AiGenerationService;
import com.abhinay.buildrix.intelligence_service.service.ChatService;
import com.abhinay.buildrix.intelligence_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


@Service
@Slf4j
@RequiredArgsConstructor
public class AiGenerationServiceImpl implements AiGenerationService {

    private final ChatClient chatClient;
    private final AuthUtil authUtil;
    private final WorkspaceServiceClient workspaceServiceClient;
    private final FileTreeAdvisor fileTreeAdvisor;
    private final ChatService chatService;
    private final UsageService usageService;

    //private static final Pattern FILE_TAG_PATTERN = Pattern.compile("<file path = \"([^\"]+)\">(.*?)</file>", Pattern.DOTALL);

    @PreAuthorize("@security.canEditProject(#projectId)")
    @Override
    public Flux<ChatStreamResponse> streamResponse(String message, UUID projectId) {

        UUID userId = authUtil.getCurrentUserId();
        usageService.checkDailyTokensUsage();

        ChatSession chatSession = createChatSessionIfNotExists(projectId, userId);
        Map<String, Object> advisorParam = Map.of(
                "user_id", userId,
                "project_id", projectId
        );

        StringBuilder bufferedResponse = new StringBuilder();
        CodeGenerationTool codeGenerationTool = new CodeGenerationTool(projectId, workspaceServiceClient);
        AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
        AtomicReference<Long> endTime = new AtomicReference<>(0L);
        AtomicReference<Usage> usageRef = new AtomicReference<>();
        return chatClient.
                prompt()
                .tools(codeGenerationTool)
                .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
                .user(message)
                .advisors(
                        advisorSpec -> {
                            advisorSpec.params(advisorParam);
                            advisorSpec.advisors(fileTreeAdvisor, new SimpleLoggerAdvisor());
                        }
                ).stream()
                .chatResponse()
                .doOnNext(chatResponse -> {

                    String content = Objects.requireNonNull(
                            chatResponse.getResult()).getOutput().getText();
                    if(content != null && !content.isEmpty() && endTime.get() == 0) { // first non-empty chunk received
                        endTime.set(System.currentTimeMillis());
                    }
                    if(chatResponse.getMetadata().getUsage() != null) {
                        usageRef.set(chatResponse.getMetadata().getUsage());
                    }

                    bufferedResponse.append(content);
                })
                .doOnComplete(() -> {
                            Schedulers.boundedElastic().schedule(() ->{
                                long duration = (endTime.get() - startTime.get()) /  1000;
                                finalizeChats(message, chatSession, bufferedResponse.toString(),
                                        duration, usageRef.get());
                            });
                                    //parseAndSaveFiles(projectId, bufferedResponse.toString()));
                        }
                )
                .doOnError(error -> {
                    log.warn("Error during chat stream from LLM for projectId: {}", projectId, error);
                })
                .map(chatResponse -> new ChatStreamResponse(Objects.requireNonNull(
                        Objects.requireNonNull(chatResponse.getResult()).getOutput().getText())));

    }


    private ChatSession createChatSessionIfNotExists(UUID projectId, UUID userId) {
        return chatService.createChatSessionIfNotExists(projectId, userId);
    }

    private void finalizeChats(String userMessage,
                               ChatSession chatSession,
                               String fullText, Long duration, Usage usage) {
        chatService.finalizeChats(userMessage,
                 chatSession,
                 fullText,  duration,  usage);
    }
}