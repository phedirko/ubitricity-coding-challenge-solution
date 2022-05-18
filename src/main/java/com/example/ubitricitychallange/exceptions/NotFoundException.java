package com.example.ubitricitychallange.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "entity not found")
public class NotFoundException extends UbitricityClientException {
    public NotFoundException(String entityName, Object id) {
        super(entityName + " with identifier: " + id.toString() + " not found");
    }
}
