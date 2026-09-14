package com.abhinay.buildrix.common_lib.events;

import java.util.UUID;

public record FileStoreRequestEvent(
        UUID projectId,
        String sagaId,
        String filePath,
        String content,
        UUID userId
) {}