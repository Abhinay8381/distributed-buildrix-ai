package com.abhinay.buildrix.account_service.service.impl;

import com.abhinay.buildrix.account_service.dto.auth.UserProfileResponse;
import com.abhinay.buildrix.account_service.entity.User;
import com.abhinay.buildrix.account_service.repository.UserRepository;
import com.abhinay.buildrix.account_service.service.UserService;
import com.abhinay.buildrix.common_lib.exceptions.ResourceNotFoundException;
import com.abhinay.buildrix.common_lib.security.JwtUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserProfileResponse getUserProfile(UUID id) {

        return null;
    }

    @Override
    public Optional<User> getUserById(UUID uuid) {
        return userRepository.findById(uuid);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        return new JwtUserPrincipal(user.getEmail(),
                user.getId(),
                user.getPassword(),
                user.getName());
    }
}
