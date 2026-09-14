package com.abhinay.buildrix.account_service.dto.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserProfileResponse user
) {
}

