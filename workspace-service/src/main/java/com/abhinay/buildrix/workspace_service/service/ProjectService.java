package com.abhinay.buildrix.workspace_service.service;


import com.abhinay.buildrix.common_lib.enums.ProjectPermissions;
import com.abhinay.buildrix.workspace_service.dto.project.ProjectRequest;
import com.abhinay.buildrix.workspace_service.dto.project.ProjectResponse;
import com.abhinay.buildrix.workspace_service.dto.project.ProjectSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    List<ProjectSummaryResponse> getAllUserProjects();

    ProjectSummaryResponse getProjectById(UUID id);

    ProjectResponse createProject(ProjectRequest projectRequest);

    void softDeleteProject(UUID id);

    ProjectResponse updateProject(UUID id, ProjectRequest projectRequest);

    Boolean hasPermission(UUID projectId, ProjectPermissions permission);
}
