package com.abhinay.buildrix.workspace_service.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank @Size(min = 3, max = 50) String name
) {
}
