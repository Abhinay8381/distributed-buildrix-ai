package com.abhinay.buildrix.common_lib.exceptions;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String identifier;
    private final String resource;

    public ResourceNotFoundException(String resource, String identifier) {
        this.identifier = identifier;
        this.resource = resource;
        super(resource + " not found with identifier: "+identifier);
    }

}
