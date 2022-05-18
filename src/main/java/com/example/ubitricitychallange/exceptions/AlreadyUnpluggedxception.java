package com.example.ubitricitychallange.exceptions;

public class AlreadyUnpluggedxception extends UbitricityClientException {
    public AlreadyUnpluggedxception(Object cpId) {
        super("Charging point with id: " + cpId.toString() + " already disconnected");
    }
}
