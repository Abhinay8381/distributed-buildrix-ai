package com.abhinay.buildrix.account_service.service;

import com.abhinay.buildrix.account_service.dto.auth.AuthResponse;
import com.abhinay.buildrix.account_service.dto.auth.LoginRequest;
import com.abhinay.buildrix.account_service.dto.auth.RefreshTokenRequest;
import com.abhinay.buildrix.account_service.dto.auth.SignUpRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    AuthResponse signup(SignUpRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);
}

