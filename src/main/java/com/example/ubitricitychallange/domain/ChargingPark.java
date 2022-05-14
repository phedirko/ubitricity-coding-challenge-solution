package com.example.ubitricitychallange.domain;

import com.example.ubitricitychallange.exceptions.AllChargingPointOccupiedException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// TODO: naming conventions (Capital first letter?)

public class ChargingPark {
    public ChargingPark(Set<ChargingPoint> chargingPoints, double maxAvailableCurrentAmp){
        this.chargingPoints = chargingPoints;
        this.maxAvailableCurrentAmp = maxAvailableCurrentAmp;
    }

    private Set<ChargingPoint> chargingPoints;
    private double availableCurrent; // move to the charging point?
    private double maxAvailableCurrentAmp;

    public void connect(String clientId, LocalDateTime connectedAt, int cpId) { // todo: Probably there should be a param of type Connection or similar
        var targetCP = getAvailableChargingPoints()
                .stream()
                .filter(x -> x.getId() == cpId)
                .findFirst();

        if (targetCP.isEmpty()) {
            throw new AllChargingPointOccupiedException(); // terminating if there is an EV already connected to this CP
        }

        if (calculateMaxPossibleCurrentAvailable() >= Constants.FAST_CHARGING_CURRENT_AMP) {
            connectForFastCharging(targetCP.get(), clientId, connectedAt);
        } else { // if there was a plug, there certainly would be a capacity to charge slowly
            targetCP.get().connect(clientId, connectedAt, false);
        }
    }

    public void disconnect(String clientId, LocalDateTime disconnectedAt, int cpId) {
        var targetCP = getAvailableChargingPoints()
                .stream()
                .filter(x -> x.getId() == cpId)
                .findFirst();

        if (targetCP.isEmpty()){
            throw new RuntimeException("Already disconnected");
        }

        targetCP.get().disconnect(disconnectedAt);

        // After EV disconnected, there should be an additional capacity to switch one connected EV to the fast charging
        var latestConnectedCP = getPluggedChargingPoints()
                .stream()
                .filter(x -> !x.isFastCharging())
                .sorted(Comparator.comparing(ChargingPoint::getPluggedAt).reversed())
                .findFirst();

        if (latestConnectedCP.isEmpty()) { // This shouldn't be the case, but as it doesn't break anything, no need to throw an exception
            return;
        }

        connectForFastCharging(latestConnectedCP.get(), clientId, disconnectedAt);
    }

    private void connectForFastCharging(ChargingPoint cp, String clientId, LocalDateTime connectedAt) {
        var pluggedCPs = getPluggedChargingPoints();

        double currentConsumption = pluggedCPs
                .stream()
                .map(x -> x.isFastCharging()
                        ? Constants.FAST_CHARGING_CURRENT_AMP
                        : Constants.SLOW_CHARGING_CURRENT_AMP)
                .collect(Collectors.summingDouble(Double::doubleValue));

        if (currentConsumption >= Constants.FAST_CHARGING_CURRENT_AMP) {
            cp.connect(clientId, connectedAt, true);
            return;
        }

        // todo: TESTING - test for null exception
        var fastChargingCPWithOldestConnection = pluggedCPs
                .stream()
                .filter(x -> x.EVPlugged() &&
                             x.isFastCharging())
                .sorted(Comparator.comparing(ChargingPoint::getPluggedAt)) // TODO: check order
                .findFirst()
                .get();

        // Switching the earliest connected vehicle to a slow charging
        fastChargingCPWithOldestConnection.switchToSlowCharging();
        cp.connect(clientId, connectedAt, true);
    }

    private double calculateMaxPossibleCurrentAvailable() { // todo: a better naming
        var pluggedCPsCount = getPluggedChargingPoints().size();
        double currentAvailableToReserve = maxAvailableCurrentAmp - (pluggedCPsCount * Constants.SLOW_CHARGING_CURRENT_AMP);

        return currentAvailableToReserve;
    }

    private List<ChargingPoint> getAvailableChargingPoints() {
        return chargingPoints
                .stream()
                .filter(x -> !x.EVPlugged())
                .collect(Collectors.toUnmodifiableList());
    }

    private Collection<ChargingPoint> getPluggedChargingPoints() {
        return chargingPoints
                .stream()
                .filter(x -> x.EVPlugged())
                .collect(Collectors.toUnmodifiableList());
    }
}
