package com.abhinay.buildrix.account_service.repository;

import com.abhinay.buildrix.account_service.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, UUID> {
    Optional<Plan> findByStripePriceId(String id);
}
