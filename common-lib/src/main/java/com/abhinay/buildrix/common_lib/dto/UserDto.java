package com.abhinay.buildrix.common_lib.dto;

import java.util.UUID;

public record UserDto(
        String email,
        UUID id,
        String name
) {
}
