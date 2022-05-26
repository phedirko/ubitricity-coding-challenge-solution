package com.example.ubitricitychallange.exceptions;

public class AlreadyUnpluggedException extends UbitricityClientException {
    public AlreadyUnpluggedException(Object cpId) {
        super("Charging point with id: " + cpId.toString() + " already disconnected");
    }
}
