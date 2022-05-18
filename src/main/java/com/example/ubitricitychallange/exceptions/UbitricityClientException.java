package com.example.ubitricitychallange.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Already disconnected")
public class UbitricityClientException extends RuntimeException {
    public UbitricityClientException(String message) {
        super(message);
    }
}
