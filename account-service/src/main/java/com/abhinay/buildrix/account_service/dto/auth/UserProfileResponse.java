package com.abhinay.buildrix.account_service.dto.auth;

import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String name,
        String avatarUrl,
        String email
) {
}
