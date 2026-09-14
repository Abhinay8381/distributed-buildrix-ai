package com.abhinay.buildrix.intelligence_service.llm;

import com.abhinay.buildrix.common_lib.enums.ChatEventStatus;
import com.abhinay.buildrix.common_lib.enums.ChatEventType;
import com.abhinay.buildrix.intelligence_service.entity.ChatEvent;
import com.abhinay.buildrix.intelligence_service.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class LLMResponseParser {

    /**
     * Regex Breakdown:
     * Group 1: Opening Tag (<tag ...>)
     * Group 2: Tag Name (message|file|tool)
     * Group 3: Attributes part (e.g., ' path="foo"' or ' args="a,b"')
     * Group 4: Content (The stuff inside)
     * Group 5: Closing Tag (</tag>)
     */

    private static final Pattern GENERIC_TAG_PATTERN = Pattern.compile(
            "(<(message|file|tool)([^>]*)>)([\\s\\S]*?)(</\\2>)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // Helper to extract specific attributes (path="..." or args="...") from Group 3
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
            "(path|args)=\"([^\"]+)\""
    );

    public List<ChatEvent> parseChatEvents(String fullResponse, ChatMessage parentMessage) {
        List<ChatEvent> chatEvents = new ArrayList<>();
        int orderCounter = 1;

        Matcher matcher = GENERIC_TAG_PATTERN.matcher(fullResponse);
        while (matcher.find()) {
            String tagName = matcher.group(2).toLowerCase();
            String attributes = matcher.group(3);
            String content = matcher.group(4).trim();

            Map<String, String> attributesMap = extractAttributes(attributes);
            ChatEvent.ChatEventBuilder chatEventBuilder = ChatEvent.builder()
                    .status(ChatEventStatus.CONFIRMED)
                    .sequenceOrder(orderCounter ++)
                    .chatMessage(parentMessage)
                    .content(content);

            switch (tagName){
                case "message" -> chatEventBuilder.type(ChatEventType.MESSAGE);
                case "tool" -> {
                    chatEventBuilder.type(ChatEventType.TOOL_LOG);
                    chatEventBuilder.metadata(attributesMap.get("args")); // Required for files
                }
                case "file" -> {
                    chatEventBuilder.type(ChatEventType.FILE_EDIT);
                    chatEventBuilder.status(ChatEventStatus.PENDING);
                    chatEventBuilder.filePath(attributesMap.get("path"));
                }
                default -> {continue;}
            }
            chatEvents.add(chatEventBuilder.build());
        }
        return chatEvents;
    }

    private Map<String, String> extractAttributes(String attributeString) {
        Map<String, String> attributes = new HashMap<>();
        if (attributeString == null) return attributes;

        Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributeString);
        while (matcher.find()) {
            attributes.put(matcher.group(1), matcher.group(2));
        }
        return attributes;
    }
}
