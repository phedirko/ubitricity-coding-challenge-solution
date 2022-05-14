package com.example.ubitricitychallange.exceptions;

public class AllChargingPointOccupiedException extends RuntimeException { // TODO: Change name to already plugged
    public AllChargingPointOccupiedException(){
        super("Currently there is no available charging point");
    }
}
