package com.abhinay.buildrix.workspace_service.controller;

import com.abhinay.buildrix.common_lib.dto.FileContentResponse;
import com.abhinay.buildrix.common_lib.dto.FileTreeResponse;
import com.abhinay.buildrix.workspace_service.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/projects/{projectId}/files")
@RequiredArgsConstructor
public class FileController {

    private final ProjectFileService projectFileService;
    private static final UUID userId = UUID.randomUUID();

    @GetMapping
    public ResponseEntity<FileTreeResponse> getProjectFiles(@PathVariable UUID projectId){
        return ResponseEntity.ok(projectFileService.getProjectFileTree(projectId));
    }

    @GetMapping("/content")
    public ResponseEntity<FileContentResponse> downloadFile(@PathVariable UUID projectId,
                                                            @RequestParam String path){
        return ResponseEntity.ok(projectFileService.getFileContent(projectId, path ));
    }
}
