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

    private final Set<ChargingPoint> chargingPoints;
    private final double maxAvailableCurrentAmp;

    public Collection<ChargingPoint> getChargingPoints(){
        return chargingPoints;
    }

    public Connection connect(NewEVConnection connection) {
        var targetCP = getAvailableChargingPoints()
                .stream()
                .filter(x -> x.getId() == connection.getChargingPointId())
                .findFirst();

        if (targetCP.isEmpty()) {
            throw new AllChargingPointOccupiedException(); // terminating if there is an EV already connected to this CP
        }

        if (calculateMaxPossibleCurrentAvailable() >= Constants.FAST_CHARGING_CURRENT_AMP) {
            return connectToFastCharging(targetCP.get(), connection.getClientId(), connection.getConnectedAt());
        }

        // if there was a plug, there certainly would be a capacity for a slow charging
        return connectToSlowCharging(targetCP.get(), connection.getClientId(), connection.getConnectedAt());
    }

    public void disconnect(LocalDateTime disconnectedAt, int chargingPointId) {
        var targetCP = getPluggedChargingPoints()
                .stream()
                .filter(x -> x.getId() == chargingPointId)
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

        latestConnectedCP.get().switchToFastCharging(); // todo: proper unit test for this case and debug
    }

    public double getTotalCurrentConsumption(){
        double totalCurrentConsumption = getPluggedChargingPoints()
                .stream()
                .map(x -> x.isFastCharging()
                        ? Constants.FAST_CHARGING_CURRENT_AMP
                        : Constants.SLOW_CHARGING_CURRENT_AMP)
                .collect(Collectors.summingDouble(Double::doubleValue));

        if (totalCurrentConsumption > maxAvailableCurrentAmp){
            throw new RuntimeException("Flaw in business logic"); // todo: special exception type
        }

        return totalCurrentConsumption;
    }

    private Connection connectToFastCharging(ChargingPoint cp, String clientId, LocalDateTime connectedAt) {
        double currentAvailable = maxAvailableCurrentAmp - getTotalCurrentConsumption();

        while (currentAvailable < Constants.FAST_CHARGING_CURRENT_AMP) {
            var fastChargingCPWithOldestConnection = getPluggedChargingPoints()
                    .stream()
                    .filter(x -> x.isFastCharging())
                    .sorted(Comparator.comparing(ChargingPoint::getPluggedAt))
                    .findFirst()
                    .get();

            // Switching the earliest connected vehicle to a slow charging
            fastChargingCPWithOldestConnection.switchToSlowCharging();
            currentAvailable = maxAvailableCurrentAmp - getTotalCurrentConsumption();
        }

        return cp.connect(clientId, connectedAt, true);
    }

    private Connection connectToSlowCharging(ChargingPoint cp, String clientId, LocalDateTime connectedAt) {
        double currentAvailable = maxAvailableCurrentAmp - getTotalCurrentConsumption();

        // With given requirements (Number of CP = 10 and max current per park = 100) that won't run more than once
        // but if we change the values of those variables the implementation won't break (or at least not here)
        while (currentAvailable < Constants.SLOW_CHARGING_CURRENT_AMP) {
            var fastChargingCPWithOldestConnection = getPluggedChargingPoints()
                    .stream()
                    .filter(x -> x.isFastCharging())
                    .sorted(Comparator.comparing(ChargingPoint::getPluggedAt)) // TODO: check order
                    .findFirst()
                    .get();

            fastChargingCPWithOldestConnection.switchToSlowCharging();
            currentAvailable = maxAvailableCurrentAmp - getTotalCurrentConsumption();
        }

        return cp.connect(clientId, connectedAt, false);
    }

    // calculates how much of 'free' current there could be if each consumer switched to a slow charging
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
