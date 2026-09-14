package com.abhinay.buildrix.workspace_service.dto.project.member;


import com.abhinay.buildrix.common_lib.enums.ProjectRole;

import java.time.Instant;
import java.util.UUID;


public record ProjectMemberResponse(
        UUID userId,
        String name,
        String email,
        Instant invitedAt,
        ProjectRole projectRole
) {
}
