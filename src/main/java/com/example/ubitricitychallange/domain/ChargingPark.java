package com.example.ubitricitychallange.domain;

import com.example.ubitricitychallange.exceptions.AllChargingPointOccupiedException;

import java.util.*;
import java.util.stream.Collectors;

// TODO: naming conventions (Capital first letter?)

public class ChargingPark {
    private static final Object lock = new Object();

    public ChargingPark Create(Set<ChargingPoint> chargingPoints) {

    }

    public ChargingPark(Set<ChargingPoint> chargingPoints){
        this.chargingPoints = chargingPoints;
    }

    private Set<ChargingPoint> chargingPoints;
    private double availableCurrent; // move to the charging point?
    private int availableChargingPoints;
    private Set<EVConnection> connectedVehicles;

    public void Connect() {
        if (getAvailableChargingPoints().isEmpty()) {
            throw new AllChargingPointOccupiedException();
        }



    }

    private double calculateMaxPossibleCurrentAvailable(){ // todo: a better naming

    }

    private Set<ChargingPoint> getAvailableChargingPoints() {
        return chargingPoints
                .stream()
                .filter(x -> !x.isOccupied())
                .collect(Collectors.toSet());
    }

    public void Connect(EVConnection ev){
        if (availableChargingPoints <= 0) {
            throw new AllChargingPointOccupiedException();
        }

        // Reserve a plug. If there is a plug, there certainly will be a current for this chargingPoint
        // Is it possible to reserve CP more than once at one time?
        var cpReserved = this.ReserveCP();

        if (!cpReserved){
            throw new AllChargingPointOccupiedException();
        }

        //boolean connectedToFastCharging = false;
        // if available current is enough for fast charging no need for additional checks
        if (availableCurrent >= FAST_CHARGING_CURRENT_AMP){
            if(ConnectAsFastCharging(ev))
                return;
        }

        var connectedFastChargingEVs = GetFastChargingEVs();

        // probably, this should be an atomic operation
        if (!connectedFastChargingEVs.isEmpty()){
            reduceCurrentForOlderConnections();
            if(ConnectAsFastCharging(ev))
                return;
        }


    }

    // TODO: re-distribute current after each connection/disconnection
    // probably it needs a better naming
    private void reduceCurrentForOlderConnections(){
        var olderConnections = GetFastChargingEVs();
        olderConnections.sort(Comparator.comparing(o -> o.getConnectedAt()));

        var oldestConnection = olderConnections.get(0);

        oldestConnection.setFastCharging(false);

        releaseCapacity(FAST_CHARGING_CURRENT_AMP - SLOW_CHARGING_CURRENT_AMP);
    }

    private List<EVConnection> GetFastChargingEVs(){
        return this.connectedVehicles
                   .stream()
                   .filter(x -> x.isFastCharging())
                   .collect(Collectors.toList());
    }

    private boolean ConnectAsFastCharging(EVConnection ev){
        // Reserving a capacity here
        boolean capacityReservedSuccessfully = ReserveCapacity(FAST_CHARGING_CURRENT_AMP);

        if (capacityReservedSuccessfully) {
            ev.setFastCharging(true);
            connectedVehicles.add(ev);
            return true;
        }

        return false;
    }

    private void ConnectAsSlowCharging(EVConnection ev){
        if (availableCurrent >= SLOW_CHARGING_CURRENT_AMP){

        }
    }

    private boolean ReserveCP(){
        boolean reservedSuccessfully = false;
        synchronized (lock){
            if (availableChargingPoints > 0) {
                availableChargingPoints -= 1;
                reservedSuccessfully = true;
            }
        }

        return reservedSuccessfully;
    }


    // TODO:
    // 1. Reserve a plug
    // 2. Release capacity
    private boolean ReserveCapacity(double currentToReserve){
        boolean reservedSuccessfully = false;
        synchronized (lock){
            // Doublechecking the available current if someone already
            if (availableCurrent >= currentToReserve){
                availableCurrent -= currentToReserve;
                reservedSuccessfully = true;
            }
        }

        return reservedSuccessfully;
    }

    // probably move it
    // to sync it with setFastCharging=false
    private void releaseCapacity(double currentToRelease){
        // sync?
        availableCurrent += currentToRelease;
    }


}
