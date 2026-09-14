package com.abhinay.buildrix.account_service.dto.auth;

public record LoginRequest(
        String email,
        String password
) {
}
