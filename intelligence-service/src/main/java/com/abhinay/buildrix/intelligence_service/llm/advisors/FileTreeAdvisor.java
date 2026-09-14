package com.abhinay.buildrix.intelligence_service.llm.advisors;

import com.abhinay.buildrix.common_lib.dto.FileNode;
import com.abhinay.buildrix.intelligence_service.client.WorkspaceServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class FileTreeAdvisor implements StreamAdvisor {

    private final WorkspaceServiceClient workspaceServiceClient;

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request,
                                                 StreamAdvisorChain streamAdvisorChain) {
        Map<String, Object> context = request.context();
        UUID projectId = context.get("project_id") != null
                ? UUID.fromString(context.get("project_id").toString()) : null;
        ChatClientRequest augmentedRequest = augmentRequestWithFileTree(request, projectId);
        return streamAdvisorChain.nextStream(augmentedRequest);
    }

    private ChatClientRequest augmentRequestWithFileTree(ChatClientRequest request, UUID projectId) {

        List<Message> incomingMessages = request.prompt().getInstructions();
        Message systemMessage = incomingMessages.stream()
                        .filter(message -> message.getMessageType() == MessageType.SYSTEM)
                                .findFirst()
                                        .orElse(null);

        List<Message> userMessages = incomingMessages.stream()
                        .filter(message -> message.getMessageType() != MessageType.SYSTEM)
                                .toList();

        List<Message> allMessages = new ArrayList<>();

        if(systemMessage != null)
            allMessages.add(systemMessage);

        List<FileNode> fileTreeNodes =  workspaceServiceClient.getProjectFileTree(projectId).files();
        String fileTreeContext = "\n\n ----- FILE_TREE ----\n" + fileTreeNodes.toString();

        allMessages.add(new SystemMessage(fileTreeContext));

        allMessages.addAll(userMessages);

        return request.mutate()
                .prompt(new Prompt(allMessages, request.prompt().getOptions()))
                .build();
    }

    @Override
    public String getName() {
        return "FileTreeAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
