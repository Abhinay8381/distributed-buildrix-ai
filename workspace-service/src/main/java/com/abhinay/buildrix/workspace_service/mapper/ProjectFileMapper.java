package com.abhinay.buildrix.workspace_service.mapper;

import com.abhinay.buildrix.common_lib.dto.FileNode;
import com.abhinay.buildrix.workspace_service.entity.ProjectFile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectFileMapper {

    FileNode toFileNode(ProjectFile projectFile);

    List<FileNode> toListOfFileNode(List<ProjectFile> projectFileList);
}

