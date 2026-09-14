package com.abhinay.buildrix.workspace_service.service;

import java.util.UUID;

public interface ProjectTemplateService {
    void initializeProjectFromTemplate(UUID projectId);
}
