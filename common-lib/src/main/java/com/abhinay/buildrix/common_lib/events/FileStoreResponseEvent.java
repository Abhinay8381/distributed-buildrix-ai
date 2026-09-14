package com.abhinay.buildrix.common_lib.events;

import lombok.Builder;

import java.util.UUID;

@Builder
public record FileStoreResponseEvent(
        String sagaId,
        boolean success,
        String errorMessage,
        UUID projectId
) {}