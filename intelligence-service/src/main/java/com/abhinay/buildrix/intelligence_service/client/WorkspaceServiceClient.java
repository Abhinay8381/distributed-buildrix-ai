package com.abhinay.buildrix.intelligence_service.client;

import com.abhinay.buildrix.common_lib.dto.FileContentResponse;
import com.abhinay.buildrix.common_lib.dto.FileTreeResponse;
import com.abhinay.buildrix.common_lib.enums.ProjectPermissions;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "workspace-service", path = "/workspace/internal/project")
public interface WorkspaceServiceClient {

    @GetMapping("/{projectId}/file-tree")
     FileTreeResponse getProjectFileTree(@PathVariable("projectId") UUID projectId);

    @GetMapping("/{projectId}/file-content")
    FileContentResponse getFileContent(@PathVariable("projectId")UUID projectId, @RequestParam("path") String path);

    @GetMapping("/{projectId}/has-permission")
    Boolean hasPermission(@PathVariable("projectId") UUID projectId, @RequestParam("permission") ProjectPermissions permission);
}
