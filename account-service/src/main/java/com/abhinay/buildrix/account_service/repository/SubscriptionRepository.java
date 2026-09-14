package com.abhinay.buildrix.account_service.repository;

import com.abhinay.buildrix.account_service.entity.Subscription;
import com.abhinay.buildrix.common_lib.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByUserIdAndStatusIn(UUID userId, Set<SubscriptionStatus> active);

    boolean existsByGatewaySubscriptionId(String subscriptionId);

    Optional<Subscription> findByGatewaySubscriptionId(String gatewaySubId);
}
