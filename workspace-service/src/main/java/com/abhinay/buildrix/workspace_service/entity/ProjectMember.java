package com.abhinay.buildrix.workspace_service.entity;

import com.abhinay.buildrix.common_lib.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "project_members")
public class ProjectMember {

    @EmbeddedId
    private ProjectMemberId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role;

    private Instant invitedAt;
    private Instant acceptedAt;

    @ManyToOne
    @MapsId("projectId")
    private Project project;

}
