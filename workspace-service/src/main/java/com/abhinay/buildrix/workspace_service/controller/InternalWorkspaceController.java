package com.abhinay.buildrix.workspace_service.controller;

import com.abhinay.buildrix.common_lib.dto.FileContentResponse;
import com.abhinay.buildrix.common_lib.dto.FileTreeResponse;
import com.abhinay.buildrix.common_lib.enums.ProjectPermissions;
import com.abhinay.buildrix.workspace_service.service.ProjectFileService;
import com.abhinay.buildrix.workspace_service.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/project")
public class InternalWorkspaceController {

    private final ProjectFileService projectFileService;
    private final ProjectService projectService;

    @GetMapping("/{projectId}/file-tree")
    public FileTreeResponse getProjectFileTree(@PathVariable("projectId")UUID projectId){
        return projectFileService.getProjectFileTree(projectId);
    }

    @GetMapping("/{projectId}/file-content")
    public FileContentResponse getFileContent(@PathVariable("projectId") UUID projectId, @RequestParam("path") String path){
        return projectFileService.getFileContent(projectId, path);
    }

    @GetMapping("/{projectId}/has-permission")
    Boolean hasPermission(@PathVariable("projectId") UUID projectId, @RequestParam("permission") ProjectPermissions permission){
        return projectService.hasPermission(projectId, permission);
    }

}
