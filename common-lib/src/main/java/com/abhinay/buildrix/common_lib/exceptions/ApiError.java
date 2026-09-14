package com.abhinay.buildrix.common_lib.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        HttpStatus status,
        String message,
        Instant timeStamp,
        List<FieldApiError> suberrors

) {
    public ApiError(HttpStatus status, String message) {
        this(status, message, Instant.now(), null);
    }
    public ApiError(HttpStatus status, String message, List<FieldApiError> suberrors) {
        this(status, message, Instant.now(), suberrors);
    }
}
record FieldApiError(String field, String message){}
