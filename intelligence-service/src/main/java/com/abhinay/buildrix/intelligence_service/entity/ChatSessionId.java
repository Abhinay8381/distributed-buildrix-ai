package com.abhinay.buildrix.intelligence_service.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ChatSessionId {

    private UUID userId;
    private UUID projectId;
}
