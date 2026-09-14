package com.abhinay.buildrix.common_lib.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static com.abhinay.buildrix.common_lib.enums.ProjectPermissions.*;

@RequiredArgsConstructor
@Getter
public enum ProjectRole {
    EDITOR(VIEW, EDIT, VIEW_MEMBERS),
    VIEWER(VIEW, VIEW_MEMBERS),
    OWNER(VIEW, EDIT,
            DELETE, MANAGE_MEMBERS, VIEW_MEMBERS);

     ProjectRole(ProjectPermissions... permission){
        this.permissions = Set.of(permission);
    }


    private final Set<ProjectPermissions> permissions;
}
