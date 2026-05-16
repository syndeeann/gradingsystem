package com.gradingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String resource, String field, Object value) {
        super(resource + " already exists with " + field + ": " + value);
    }

    public DuplicateResourceException(String message) {
        super(message);
    }
}
