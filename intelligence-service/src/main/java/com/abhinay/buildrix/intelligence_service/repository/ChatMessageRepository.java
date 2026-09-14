package com.abhinay.buildrix.intelligence_service.repository;

import com.abhinay.buildrix.intelligence_service.entity.ChatMessage;
import com.abhinay.buildrix.intelligence_service.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("""
        SELECT DISTINCT cm FROM ChatMessage cm
        LEFT JOIN FETCH cm.events e
        WHERE cm.chatSession = :chatSession
        ORDER BY cm.createdAt ASC, e.sequenceOrder ASC""")
    List<ChatMessage> findChatMessagesByChatSession(@Param("chatSession") ChatSession chatSession);
}
