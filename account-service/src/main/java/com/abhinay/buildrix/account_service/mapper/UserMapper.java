package com.abhinay.buildrix.account_service.mapper;

import com.abhinay.buildrix.account_service.dto.auth.SignUpRequest;
import com.abhinay.buildrix.account_service.dto.auth.UserProfileResponse;
import com.abhinay.buildrix.account_service.entity.User;
import com.abhinay.buildrix.common_lib.dto.UserDto;
import com.abhinay.buildrix.common_lib.security.JwtUserPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(source = "userId", target = "id")
    UserProfileResponse toUserProfileResponse(JwtUserPrincipal user);
    User toEntity(SignUpRequest request);
    UserDto toUserDto(User user);
}
