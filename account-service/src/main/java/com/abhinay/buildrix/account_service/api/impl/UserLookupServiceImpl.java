package com.abhinay.buildrix.account_service.api.impl;

import com.abhinay.buildrix.account_service.api.UserLookupService;
import com.abhinay.buildrix.account_service.mapper.UserMapper;
import com.abhinay.buildrix.account_service.repository.UserRepository;
import com.abhinay.buildrix.common_lib.dto.UserDto;
import com.abhinay.buildrix.common_lib.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserLookupServiceImpl implements UserLookupService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toUserDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    @Override
    public UserDto findById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toUserDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
    }

    @Override
    public List<UserDto> findByIds(Set<UUID> ids) {
        return userRepository.findAllById(ids)
                .stream()
                .map(userMapper::toUserDto)
                .toList();
    }
}
