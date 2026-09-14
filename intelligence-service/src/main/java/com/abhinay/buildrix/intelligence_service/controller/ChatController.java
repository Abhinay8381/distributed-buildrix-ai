package com.abhinay.buildrix.intelligence_service.controller;


import com.abhinay.buildrix.intelligence_service.dto.chat.ChatRequest;
import com.abhinay.buildrix.intelligence_service.dto.chat.ChatResponse;
import com.abhinay.buildrix.intelligence_service.dto.chat.ChatStreamResponse;
import com.abhinay.buildrix.intelligence_service.service.AiGenerationService;
import com.abhinay.buildrix.intelligence_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final AiGenerationService aiGenerationService;
    private final ChatService chatService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    private Flux<ServerSentEvent<ChatStreamResponse>> streamChat(@RequestBody ChatRequest chatRequest){

        return aiGenerationService.streamResponse(chatRequest.message(), chatRequest.projectId())
                .map(message -> ServerSentEvent.<ChatStreamResponse>builder()
                        .data(message)
                        .build());
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<ChatResponse>> getChatHistory(
            @PathVariable UUID projectId) {

        return ResponseEntity.ok(chatService.getProjectChatHistory(projectId));
    }
}
