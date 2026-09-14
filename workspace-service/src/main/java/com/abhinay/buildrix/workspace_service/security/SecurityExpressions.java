package com.abhinay.buildrix.workspace_service.security;

import com.abhinay.buildrix.common_lib.enums.ProjectPermissions;
import com.abhinay.buildrix.common_lib.security.AuthUtil;
import com.abhinay.buildrix.workspace_service.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component("security")
public class SecurityExpressions {

    private final AuthUtil authUtil;
    private final ProjectMemberRepository projectMemberRepository;


        public boolean hasPermission(UUID projectId, ProjectPermissions permission){
            UUID userId = authUtil.getCurrentUserId();
            return projectMemberRepository.findProjectRoleByMemberIdAndProjectId(userId, projectId)
                    .map((role) -> role.getPermissions().contains(permission))
                    .orElse(false);
        }
        public boolean canViewProject(UUID projectId){
        return hasPermission(projectId, ProjectPermissions.VIEW);
    }

    public boolean canEditProject(UUID projectId){
        return hasPermission(projectId, ProjectPermissions.EDIT);
    }

    public boolean canDeleteProject(UUID projectId){
        return hasPermission(projectId, ProjectPermissions.DELETE);
    }

    public boolean canViewMembers(UUID projectId){
        return hasPermission(projectId, ProjectPermissions.VIEW_MEMBERS);
    }

    public boolean canManageMembers(UUID projectId){
        return hasPermission(projectId, ProjectPermissions.MANAGE_MEMBERS);
    }
}
