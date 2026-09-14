package com.abhinay.buildrix.intelligence_service.repository;

import com.abhinay.buildrix.intelligence_service.entity.ChatSession;
import com.abhinay.buildrix.intelligence_service.entity.ChatSessionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, ChatSessionId> {

}
