package com.abhinay.buildrix.workspace_service.entity;

import com.abhinay.buildrix.common_lib.entity.BaseEntity;
import com.abhinay.buildrix.common_lib.enums.PreviewStatus;
import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Preview extends BaseEntity {

    private Project project;
    private String namespace;
    private String podName;
    private String previewUrl;
    private Instant startedAt;
    private Instant terminatedAt;
    private PreviewStatus status;
}
