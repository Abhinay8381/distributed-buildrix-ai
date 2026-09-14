package com.abhinay.buildrix.workspace_service.dto.project.member;

import com.abhinay.buildrix.common_lib.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectMemberRequest(
        @NotNull ProjectRole role
) {
}
