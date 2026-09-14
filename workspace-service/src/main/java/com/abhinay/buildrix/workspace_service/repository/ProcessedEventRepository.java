package com.abhinay.buildrix.workspace_service.repository;

import com.abhinay.buildrix.workspace_service.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}
