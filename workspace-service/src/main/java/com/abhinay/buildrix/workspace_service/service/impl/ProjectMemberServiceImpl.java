package com.abhinay.buildrix.workspace_service.service.impl;

import com.abhinay.buildrix.common_lib.dto.UserDto;
import com.abhinay.buildrix.common_lib.exceptions.ResourceNotFoundException;
import com.abhinay.buildrix.common_lib.security.AuthUtil;
import com.abhinay.buildrix.workspace_service.client.AccountServiceClient;
import com.abhinay.buildrix.workspace_service.dto.project.member.InviteMemberRequest;
import com.abhinay.buildrix.workspace_service.dto.project.member.ProjectMemberResponse;
import com.abhinay.buildrix.workspace_service.dto.project.member.UpdateProjectMemberRequest;
import com.abhinay.buildrix.workspace_service.entity.Project;
import com.abhinay.buildrix.workspace_service.entity.ProjectMember;
import com.abhinay.buildrix.workspace_service.entity.ProjectMemberId;
import com.abhinay.buildrix.workspace_service.mapper.ProjectMemberMapper;
import com.abhinay.buildrix.workspace_service.repository.ProjectMemberRepository;
import com.abhinay.buildrix.workspace_service.repository.ProjectRepository;
import com.abhinay.buildrix.workspace_service.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberMapper projectMemberMapper;
    private final AccountServiceClient accountServiceClient;
    private final AuthUtil authUtil;

    @PreAuthorize("@security.canViewMembers(#projectId)")
    @Override
    public List<ProjectMemberResponse> getAllProjectMembers(UUID projectId) {
        List<ProjectMember> projectMembers = projectMemberRepository.findById_ProjectId(projectId);

        Set<UUID> memberIds = projectMembers.stream()
                .map(pm -> pm.getId().getMemberId())
                .collect(Collectors.toSet());
        Map<UUID, UserDto> members = accountServiceClient.findUserByIds(memberIds)
                .stream()
                .collect(Collectors.toMap(
                        UserDto::id,
                        Function.identity()
                ));
        return projectMembers
                .stream()
                .map(pm ->
                        projectMemberMapper.toProjectMemberResponseFromProjectMember(
                                pm, members.get(pm.getId().getMemberId())
                        ))
                .toList();
    }

    @PreAuthorize("@security.canManageMembers(#projectId)")
    @Transactional
    @Override
    public ProjectMemberResponse inviteMember(UUID projectId, InviteMemberRequest request) {
        UUID userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(userId, projectId);

        UserDto invitee = accountServiceClient.findUserByEmail(request.email());

        if(userId.equals(invitee.id()))
            throw new RuntimeException("Cannot invite yourself");

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, invitee.id());
        if(projectMemberRepository.existsById(projectMemberId))
            throw new RuntimeException(
                    "User is already a project member"
            );

        ProjectMember projectMember = ProjectMember.builder()
                .id(projectMemberId)
                .role(request.role())
                .invitedAt(Instant.now())
                .project(project)
                .build();
        projectMemberRepository.save(projectMember);
        return projectMemberMapper.toProjectMemberResponseFromProjectMember(projectMember, invitee);
    }

    @Transactional
    @PreAuthorize("@security.canManageMembers(#projectId)")
    @Override
    public ProjectMemberResponse updateMemberRole(UUID projectId, UUID memberId, UpdateProjectMemberRequest request) {
        UUID userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(userId, projectId);

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        ProjectMember projectMember = projectMemberRepository.findById(projectMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("Project Member", memberId.toString()));
        UserDto user = accountServiceClient.findUserById(userId);
        projectMember.setRole(request.role());
        projectMemberRepository.save(projectMember);
        return projectMemberMapper.toProjectMemberResponseFromProjectMember(projectMember, user);
    }

    @Transactional
    @PreAuthorize("@security.canManageMembers(#projectId)")
    @Override
    public void removeMember(UUID projectId, UUID memberId) {
        UUID userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(userId, projectId);

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        if(!projectMemberRepository.existsById(projectMemberId))
            throw new RuntimeException(
                    "User is not a project member"
            );
        projectMemberRepository.deleteById(projectMemberId);
    }

    private Project getAccessibleProjectById(UUID ownerId, UUID id) {
        return projectRepository.findAccessibleProjectById(ownerId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id.toString()));
    }
}
