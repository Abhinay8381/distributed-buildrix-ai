package com.abhinay.buildrix.workspace_service.mapper;

import com.abhinay.buildrix.common_lib.dto.UserDto;
import com.abhinay.buildrix.workspace_service.dto.project.member.ProjectMemberResponse;
import com.abhinay.buildrix.workspace_service.entity.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectMemberMapper {

    @Mapping(target = "projectRole", constant = "OWNER")
    @Mapping(target = "userId", source = "id")
    ProjectMemberResponse toProjectMemberResponseFromOwner(UserDto user);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "projectRole", source = "projectMember.role")
    ProjectMemberResponse toProjectMemberResponseFromProjectMember(ProjectMember projectMember, UserDto user);
}
