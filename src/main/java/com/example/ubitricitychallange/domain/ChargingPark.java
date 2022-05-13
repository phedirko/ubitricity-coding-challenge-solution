package com.example.ubitricitychallange.domain;

import com.example.ubitricitychallange.exceptions.AllChargingPointOccupiedException;

import java.util.*;
import java.util.stream.Collectors;

// TODO: naming conventions (Capital first letter?)

public class ChargingPark {
    private static final Object lock = new Object();
    private static final double MAX_AVAILABLE_CURRENT_AMP = 100;
    private static final double FAST_CHARGING_CURRENT_AMP = 20;
    private static final double SLOW_CHARGING_CURRENT_AMP = 10;

    private static final int CHARGING_POINTS_NUMBER = 10;

    public ChargingPark(){
        this.connectedVehicles = new HashSet<EVConnection>();
        this.availableCurrent = MAX_AVAILABLE_CURRENT_AMP;
        this.availableChargingPoints = CHARGING_POINTS_NUMBER;
    }

    private double availableCurrent; // move to the charging point?
    private int availableChargingPoints;
    private Set<EVConnection> connectedVehicles;

    public void Connect(EVConnection ev){
        if (availableChargingPoints <= 0) {
            throw new AllChargingPointOccupiedException();
        }

        // Reserve a plug. If there is a plug, there certainly will be a current for this chargingPoint
        var cpReserved = this.ReserveCP();

        if (!cpReserved){
            throw new AllChargingPointOccupiedException();
        }

        // if available current is enough for fast charging no need for additional checks
        if (availableCurrent >= FAST_CHARGING_CURRENT_AMP){
            ConnectAsFastCharging(ev);
        }

        var connectedFastChargingEVs = GetFastChargingEVs();

        // probably, this should be an atomic operation
        if (!connectedFastChargingEVs.isEmpty()){
            reduceCurrentForOlderConnections();
            ConnectAsFastCharging(ev);
        }
    }

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
