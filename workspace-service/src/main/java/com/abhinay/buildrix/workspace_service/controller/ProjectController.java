package com.abhinay.buildrix.workspace_service.controller;

import com.abhinay.buildrix.workspace_service.dto.deploy.DeploymentResponse;
import com.abhinay.buildrix.workspace_service.dto.project.ProjectRequest;
import com.abhinay.buildrix.workspace_service.dto.project.ProjectResponse;
import com.abhinay.buildrix.workspace_service.dto.project.ProjectSummaryResponse;
import com.abhinay.buildrix.workspace_service.service.DeploymentService;
import com.abhinay.buildrix.workspace_service.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private static final UUID userId = UUID.fromString("32aea559-aed5-45c5-bfec-23dcc3ef70c5");

    private final ProjectService projectService;
    private final DeploymentService deploymentService;

    @GetMapping()
    public ResponseEntity<List<ProjectSummaryResponse>> getAllProjects(){
        return ResponseEntity.ok(projectService.getAllUserProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectSummaryResponse> getProject(@PathVariable UUID id){
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping()
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest projectRequest){
        return ResponseEntity.status(201)
                .body(projectService.createProject(projectRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id){
        projectService.softDeleteProject(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@Valid @RequestBody  ProjectRequest projectRequest,
                                                         @PathVariable UUID id){
        return ResponseEntity.ok(projectService.updateProject(id, projectRequest));
    }

    @PostMapping("/{id}/deploy")
    public ResponseEntity<DeploymentResponse> deployProject(@PathVariable UUID id) {
        return ResponseEntity.ok(deploymentService.deploy(id));
    }

}
