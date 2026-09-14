package com.abhinay.buildrix.workspace_service.service.impl;

import com.abhinay.buildrix.common_lib.dto.FileContentResponse;
import com.abhinay.buildrix.common_lib.dto.FileTreeResponse;
import com.abhinay.buildrix.common_lib.exceptions.ResourceNotFoundException;
import com.abhinay.buildrix.workspace_service.entity.Project;
import com.abhinay.buildrix.workspace_service.entity.ProjectFile;
import com.abhinay.buildrix.workspace_service.mapper.ProjectFileMapper;
import com.abhinay.buildrix.workspace_service.repository.ProjectFileRepository;
import com.abhinay.buildrix.workspace_service.repository.ProjectRepository;
import com.abhinay.buildrix.workspace_service.service.ProjectFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectFileServiceImpl implements ProjectFileService {

    private final ProjectRepository projectRepository;
    private final MinioClient minioClient;
    private final ProjectFileRepository projectFileRepository;
    private final ProjectFileMapper projectFileMapper;

    @Value("${minio.project-bucket}")
    private String projectBucket;

    @Override
    public FileTreeResponse getProjectFileTree(UUID projectId) {
        List<ProjectFile> projectFiles = projectFileRepository.findByProjectId(projectId);
        return new FileTreeResponse(projectFileMapper.toListOfFileNode(projectFiles));
    }

    @Override
    public FileContentResponse getFileContent(UUID projectId, String filePath) {
        String objectName = projectId + "/" + filePath;

        try {
            InputStream is = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(projectBucket)
                            .object(objectName)
                            .build());

            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new FileContentResponse(filePath, content);
        } catch (Exception e) {
            log.warn("Failed to read file {}/{} content", projectId, filePath);
            throw new RuntimeException("Failed to read file Content", e);
        }

    }

    @Override
    public void saveFile(UUID projectId, String filePath, String content) {
        log.info("Saving file: {}", filePath);
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new ResourceNotFoundException("Project", projectId.toString()));

        String cleanPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
        String objectKey = projectId + "/" + cleanPath;

        try {
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(contentBytes);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(projectBucket)
                            .object(objectKey)
                            .stream(inputStream, contentBytes.length, -1)
                            .contentType(determineContentType(filePath))
                            .build());

            ProjectFile projectFile = projectFileRepository.findByProjectIdAndPath(projectId, filePath)
                    .orElseGet(() -> ProjectFile.builder()
                            .project(project)
                            .path(cleanPath)
                            .minioObjectKey(objectKey) // Use the key we generated
                            .build());
            projectFile.setUpdatedAt(Instant.now());
            projectFileRepository.save(projectFile);
            log.info("Saved file: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to save file {}/{}", projectId, cleanPath, e);
            throw new RuntimeException("File save failed", e);
        }
    }

    private String determineContentType(String path) {
        String type = URLConnection.guessContentTypeFromName(path);
        if (type != null)
            return type;
        if (path.endsWith(".jsx") || path.endsWith(".ts") || path.endsWith(".tsx"))
            return "text/javascript";
        if (path.endsWith(".json"))
            return "application/json";
        if (path.endsWith(".css"))
            return "text/css";

        return "text/plain";
    }
}
