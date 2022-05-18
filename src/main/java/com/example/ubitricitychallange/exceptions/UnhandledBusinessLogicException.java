package com.example.ubitricitychallange.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Flaw in business logic")
public class UnhandledBusinessLogicException extends RuntimeException {
    public UnhandledBusinessLogicException() {
        super("Unexpected condition error");
    }
}
