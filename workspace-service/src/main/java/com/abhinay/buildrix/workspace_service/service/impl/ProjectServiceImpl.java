package com.abhinay.buildrix.workspace_service.service.impl;

import com.abhinay.buildrix.common_lib.dto.SubscriptionResponse;
import com.abhinay.buildrix.common_lib.dto.UserDto;
import com.abhinay.buildrix.common_lib.enums.ProjectPermissions;
import com.abhinay.buildrix.common_lib.enums.ProjectRole;
import com.abhinay.buildrix.common_lib.enums.SubscriptionStatus;
import com.abhinay.buildrix.common_lib.exceptions.BadRequestException;
import com.abhinay.buildrix.common_lib.exceptions.ResourceNotFoundException;
import com.abhinay.buildrix.common_lib.security.AuthUtil;
import com.abhinay.buildrix.workspace_service.client.AccountServiceClient;
import com.abhinay.buildrix.workspace_service.dto.project.ProjectRequest;
import com.abhinay.buildrix.workspace_service.dto.project.ProjectResponse;
import com.abhinay.buildrix.workspace_service.dto.project.ProjectSummaryResponse;
import com.abhinay.buildrix.workspace_service.entity.Project;
import com.abhinay.buildrix.workspace_service.entity.ProjectMember;
import com.abhinay.buildrix.workspace_service.entity.ProjectMemberId;
import com.abhinay.buildrix.workspace_service.mapper.ProjectMapper;
import com.abhinay.buildrix.workspace_service.repository.ProjectMemberRepository;
import com.abhinay.buildrix.workspace_service.repository.ProjectRepository;
import com.abhinay.buildrix.workspace_service.security.SecurityExpressions;
import com.abhinay.buildrix.workspace_service.service.ProjectService;
import com.abhinay.buildrix.workspace_service.service.ProjectTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final AccountServiceClient accountServiceClient;
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectMemberRepository projectMemberRepository;
    private final AuthUtil authUtil;
    private final ProjectTemplateService projectTemplateService;
    private final SecurityExpressions securityExpressions;

    private static final int FREE_TIER_PROJECT_LIMIT = 100;

    @Override
    public List<ProjectSummaryResponse> getAllUserProjects() {
        UUID userId = authUtil.getCurrentUserId();
        return projectRepository.findAllAccessibleByUser(userId)
                .stream()
                .map(p -> projectMapper.toProjectSummaryResponse(
                        p.getProject(), p.getRole()
                )).toList();
    }

    @PreAuthorize("@security.canViewProject(#id)")
    @Override
    public ProjectSummaryResponse getProjectById( UUID id) {
        UUID ownerId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(ownerId, id);
        return projectRepository.findAccessibleProjectByIdWithRole(ownerId, id)
                .map(p -> projectMapper.toProjectSummaryResponse(p.getProject(), p.getRole()))
                .orElseThrow(() -> new BadRequestException("Project not found"));
    }


    @Transactional
    @Override
    public ProjectResponse createProject(ProjectRequest projectRequest) {
        if(!canCreateProject()){
            throw new BadRequestException("Cannot create project. Project limit Exceeded. Please upgrade you plan to create more projects.");
        }
        UUID userId = authUtil.getCurrentUserId();
        Project project = Project.builder()
                .name(projectRequest.name())
                .build();
        project = projectRepository.save(project);

        ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(), userId);
        ProjectMember projectMember = ProjectMember.builder()
                .id(projectMemberId)
                .project(project)
                .role(ProjectRole.OWNER)
                .invitedAt(Instant.now())
                .acceptedAt(Instant.now())
                .build();
        projectMemberRepository.save(projectMember);

        projectTemplateService.initializeProjectFromTemplate(project.getId());
        return projectMapper.toProjectResponse(project);
    }

    @Transactional
    @Override
    @PreAuthorize("@security.canDeleteProject(#id)")
    public void softDeleteProject(UUID id) {
        UUID ownerId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(ownerId, id);
       project.setDeletedAt(Instant.now());
    }

    @PreAuthorize("@security.canEditProject(#id)")
    @Transactional
    @Override
    public ProjectResponse updateProject(UUID id, ProjectRequest projectRequest) {
        UUID ownerId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(ownerId, id);
        project.setName(projectRequest.name());
        projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public Boolean hasPermission(UUID projectId, ProjectPermissions permission) {
        return securityExpressions.hasPermission(projectId, permission);
    }


    private Project getAccessibleProjectById(UUID ownerId, UUID id) {
        return projectRepository.findAccessibleProjectById(ownerId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id.toString()));
    }

    private boolean canCreateProject() {
        Integer userProjectCount = projectMemberRepository.countProjectsByUserId(authUtil.getCurrentUserId());
        SubscriptionResponse subscriptionResponse = accountServiceClient.getUserSubscription();
        if(subscriptionResponse.plan() == null || subscriptionResponse.status().equals(SubscriptionStatus.TRAILING))
            return FREE_TIER_PROJECT_LIMIT > userProjectCount;
        return subscriptionResponse.plan().maxProjects() > userProjectCount;
    }

}
