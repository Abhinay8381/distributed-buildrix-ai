package com.abhinay.buildrix.intelligence_service.repository;

import com.abhinay.buildrix.intelligence_service.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface UsageLogRepository extends JpaRepository<UsageLog, UUID> {
    Optional<UsageLog> findByUserIdAndDate(UUID userId, LocalDate today);
}
