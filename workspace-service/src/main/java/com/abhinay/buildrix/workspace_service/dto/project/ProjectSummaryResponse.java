package com.abhinay.buildrix.workspace_service.dto.project;

import com.abhinay.buildrix.common_lib.enums.ProjectRole;

import java.time.Instant;
import java.util.UUID;

public record ProjectSummaryResponse(
        UUID id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        ProjectRole role
) {
}
