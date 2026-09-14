package com.abhinay.buildrix.workspace_service.service;


import com.abhinay.buildrix.common_lib.dto.FileContentResponse;
import com.abhinay.buildrix.common_lib.dto.FileTreeResponse;

import java.util.UUID;

public interface ProjectFileService {
    FileTreeResponse getProjectFileTree(UUID projectId);

    FileContentResponse getFileContent(UUID projectId, String filePath);

    void saveFile(UUID projectId, String filePath, String content);
}
