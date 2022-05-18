package com.example.ubitricitychallange.exceptions;

public class AlreadyPluggedException extends UbitricityClientException {
    public AlreadyPluggedException(Object cpId) {
        super("Charging point with id: " + cpId.toString() + " already connected");
    }
}
