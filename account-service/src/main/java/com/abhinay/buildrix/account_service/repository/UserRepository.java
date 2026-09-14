package com.abhinay.buildrix.account_service.repository;

import com.abhinay.buildrix.account_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByStripeCustomerId(String customerId);
}
