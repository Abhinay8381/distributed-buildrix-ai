package com.abhinay.buildrix.common_lib.dto;

import org.jspecify.annotations.NonNull;

public record FileNode(
        String path
) {

    @Override
    public @NonNull String toString(){
        return path;
    }
}
