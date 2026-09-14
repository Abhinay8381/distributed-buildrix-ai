package com.abhinay.buildrix.account_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
       @NotBlank
       @Size(max = 50, min = 3) String name,
       @Email @NotNull String email,
        @NotBlank @Size(max = 20, min = 6) String password
) {
}
