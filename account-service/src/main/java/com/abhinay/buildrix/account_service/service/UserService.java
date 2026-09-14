package com.abhinay.buildrix.account_service.service;

import com.abhinay.buildrix.account_service.dto.auth.UserProfileResponse;
import com.abhinay.buildrix.account_service.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserProfileResponse getUserProfile(UUID id);

    Optional<User> getUserById(UUID uuid);
}
