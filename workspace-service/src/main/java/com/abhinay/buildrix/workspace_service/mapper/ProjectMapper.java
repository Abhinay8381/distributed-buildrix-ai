package com.abhinay.buildrix.workspace_service.mapper;

import com.abhinay.buildrix.common_lib.enums.ProjectRole;
import com.abhinay.buildrix.workspace_service.dto.project.ProjectResponse;
import com.abhinay.buildrix.workspace_service.dto.project.ProjectSummaryResponse;
import com.abhinay.buildrix.workspace_service.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectMapper {

    ProjectResponse toProjectResponse(Project project);
    ProjectSummaryResponse toProjectSummaryResponse(Project project, ProjectRole role);
    List<ProjectSummaryResponse> toProjectSummaryResponseList(List<Project> project);
}
