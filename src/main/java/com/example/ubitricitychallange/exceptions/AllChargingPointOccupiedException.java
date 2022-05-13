package com.example.ubitricitychallange.exceptions;

public class AllChargingPointOccupiedException extends RuntimeException {
    public AllChargingPointOccupiedException(){
        super("Currently there is no available charging point");
    }
}
