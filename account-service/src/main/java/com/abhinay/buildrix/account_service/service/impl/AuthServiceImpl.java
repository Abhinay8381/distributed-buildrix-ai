package com.abhinay.buildrix.account_service.service.impl;
import com.abhinay.buildrix.account_service.dto.auth.AuthResponse;
import com.abhinay.buildrix.account_service.dto.auth.LoginRequest;
import com.abhinay.buildrix.account_service.dto.auth.RefreshTokenRequest;
import com.abhinay.buildrix.account_service.dto.auth.SignUpRequest;
import com.abhinay.buildrix.account_service.entity.User;
import com.abhinay.buildrix.account_service.mapper.UserMapper;
import com.abhinay.buildrix.account_service.repository.UserRepository;
import com.abhinay.buildrix.account_service.service.AuthService;
import com.abhinay.buildrix.common_lib.exceptions.BadRequestException;
import com.abhinay.buildrix.common_lib.exceptions.ResourceNotFoundException;
import com.abhinay.buildrix.common_lib.security.AuthUtil;
import com.abhinay.buildrix.common_lib.security.JwtUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        JwtUserPrincipal user = (JwtUserPrincipal) authentication.getPrincipal();
        return new AuthResponse(
                authUtil.generateAccessToken(user),
                authUtil.generateRefreshToken(user),
                userMapper.toUserProfileResponse(user)
        );
    }

    @Transactional
    @Override
    public AuthResponse signup(SignUpRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new BadRequestException("User with email: " + request.email() + " already exists");
        });
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        JwtUserPrincipal jwtUserPrincipal = new JwtUserPrincipal(
                user.getEmail(),
                user.getId(),
                null,
                user.getName()
        );
        return new AuthResponse(
                authUtil.generateAccessToken(jwtUserPrincipal),
                authUtil.generateRefreshToken(jwtUserPrincipal),
                userMapper.toUserProfileResponse(jwtUserPrincipal)
        );
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        JwtUserPrincipal principal = authUtil.verifyToken(request.refreshToken());
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.userId().toString()));
        JwtUserPrincipal jwtUserPrincipal = new JwtUserPrincipal(
                user.getEmail(),
                user.getId(),
                null,
                user.getName()
        );
        return new AuthResponse(
                authUtil.generateAccessToken(jwtUserPrincipal),
                authUtil.generateRefreshToken(jwtUserPrincipal),
                userMapper.toUserProfileResponse(jwtUserPrincipal)
        );
    }
}

