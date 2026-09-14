package com.abhinay.buildrix.workspace_service.controller;

import com.abhinay.buildrix.workspace_service.dto.project.member.InviteMemberRequest;
import com.abhinay.buildrix.workspace_service.dto.project.member.ProjectMemberResponse;
import com.abhinay.buildrix.workspace_service.dto.project.member.UpdateProjectMemberRequest;
import com.abhinay.buildrix.workspace_service.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;
    @GetMapping
    public ResponseEntity<List<ProjectMemberResponse>> getAllProjectMembers(@PathVariable UUID projectId){
        return ResponseEntity.ok(projectMemberService.getAllProjectMembers(projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectMemberResponse> inviteMember(@PathVariable UUID projectId,
                                        @Valid @RequestBody InviteMemberRequest request){

        return ResponseEntity.status(201)
                .body(projectMemberService.inviteMember(projectId, request));
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<ProjectMemberResponse> updateMemberRole(@PathVariable("memberId") UUID memberId,
                                                                  @PathVariable("projectId") UUID projectId,
                                                             @Valid @RequestBody UpdateProjectMemberRequest request){

        return ResponseEntity.status(200)
                .body(projectMemberService.updateMemberRole(projectId, memberId, request));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable("memberId") UUID memberId,
                                                                  @PathVariable("projectId") UUID projectId){
        projectMemberService.removeMember(projectId, memberId);
        return ResponseEntity.noContent().build();
    }
}
