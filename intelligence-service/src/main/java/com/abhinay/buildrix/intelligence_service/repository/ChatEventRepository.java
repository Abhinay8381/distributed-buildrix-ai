package com.abhinay.buildrix.intelligence_service.repository;

import com.abhinay.buildrix.intelligence_service.entity.ChatEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatEventRepository extends JpaRepository<ChatEvent, UUID> {
    Optional<ChatEvent> findBySagaId(String s);
}
