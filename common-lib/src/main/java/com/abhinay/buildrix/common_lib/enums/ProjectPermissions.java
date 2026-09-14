package com.abhinay.buildrix.common_lib.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectPermissions {
    VIEW("project:view"),
    EDIT("project:edit"),
    DELETE("project:delete"),
    MANAGE_MEMBERS("project_member:manage"),
    VIEW_MEMBERS("project_member:view");

    private final String value;
}
