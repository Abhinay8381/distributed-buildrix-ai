package com.abhinay.buildrix.workspace_service.service;

import com.abhinay.buildrix.workspace_service.dto.deploy.DeploymentResponse;

import java.util.UUID;

public interface DeploymentService {

     DeploymentResponse deploy(UUID projectId);
}
