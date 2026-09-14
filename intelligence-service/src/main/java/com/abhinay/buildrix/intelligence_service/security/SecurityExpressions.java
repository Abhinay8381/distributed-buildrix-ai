package com.abhinay.buildrix.intelligence_service.security;

import com.abhinay.buildrix.common_lib.enums.ProjectPermissions;
import com.abhinay.buildrix.common_lib.security.AuthUtil;
import com.abhinay.buildrix.intelligence_service.client.WorkspaceServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component("security")
public class SecurityExpressions {

    private final WorkspaceServiceClient workspaceServiceClient;

        private boolean hasPermission(UUID projectId, ProjectPermissions permission){
           return workspaceServiceClient.hasPermission(projectId, permission);
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
