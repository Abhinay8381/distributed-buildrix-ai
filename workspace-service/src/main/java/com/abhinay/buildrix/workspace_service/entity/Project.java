package com.abhinay.buildrix.workspace_service.entity;

import com.abhinay.buildrix.common_lib.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "projects",
indexes = {
        @Index(name = "idx_project_updated_at_desc", columnList = "updated_at desc, deleted_at"),
        @Index(name = "idx_project_deleted_at", columnList = "deleted_at"),
        @Index(name = "idx_project_deleted_at_updated_at_desc", columnList = "deleted_at, updated_at desc")
})
public class Project extends BaseEntity {

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Builder.Default
    private Boolean isPublic = false;

    private Instant deletedAt; //soft delete

}
