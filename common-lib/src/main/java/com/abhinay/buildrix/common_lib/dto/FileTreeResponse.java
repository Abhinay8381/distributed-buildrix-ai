package com.abhinay.buildrix.common_lib.dto;

import java.util.List;

public record FileTreeResponse(
        List<FileNode> files
) {
}
