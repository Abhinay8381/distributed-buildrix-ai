package com.abhinay.buildrix.account_service.api;

import com.abhinay.buildrix.common_lib.dto.UserDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserLookupService {
    UserDto findByEmail(String email);

    UserDto findById(UUID id);

    List<UserDto> findByIds(Set<UUID> ids);
}
