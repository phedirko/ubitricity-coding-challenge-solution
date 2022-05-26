package com.example.ubitricitychallange.model;

import com.example.ubitricitychallange.exceptions.AlreadyUnpluggedException;
import com.example.ubitricitychallange.exceptions.NotFoundException;
import com.example.ubitricitychallange.exceptions.UnhandledBusinessLogicException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChargingPark {
    public ChargingPark(int id, Set<ChargingPoint> chargingPoints, double maxAvailableCurrentAmp){
        this.id = id;
        this.chargingPoints = chargingPoints;
        this.maxAvailableCurrentAmp = maxAvailableCurrentAmp;
    }
    private final int id;
    private final Set<ChargingPoint> chargingPoints;
    private final double maxAvailableCurrentAmp;

    public int getId() { return id; }
    public Collection<ChargingPoint> getChargingPoints(){
        return chargingPoints;
    }

    public static ChargingPark createDefault(){
        var chargingPoints = IntStream.range(1, 11)
                .mapToObj(x -> new ChargingPoint(x))
                .collect(Collectors.toSet());

        return new ChargingPark(1, chargingPoints, Constants.MAX_AVAILABLE_CURRENT_AMP_PER_CHARGING_PARK);
    }

    public EVConnection connect(ConnectEVRequest connection) {
        var targetCP = getAvailableChargingPoints()
                .stream()
                .filter(x -> x.getId() == connection.getChargingPointId())
                .findFirst();

        if (targetCP.isEmpty()) {
            throw new NotFoundException(ChargingPoint.class.getName(), connection.getChargingPointId());
        }

        if (targetCP.get().plugged()) { // if there was no unplug request, we should enforce unplug it here
            disconnect(LocalDateTime.now(), connection.getChargingPointId());
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
            throw new AlreadyUnpluggedException(chargingPointId);
        }

        targetCP.get().unplug(disconnectedAt);

        // After EV disconnected, there should be an additional capacity to switch one connected EV to the fast charging
        var latestConnectedCP = getPluggedChargingPoints()
                .stream()
                .filter(x -> !x.isFastCharging())
                .sorted(Comparator.comparing(ChargingPoint::getPluggedAt).reversed())
                .findFirst();

        if (latestConnectedCP.isEmpty()) { // This shouldn't be the case, but as it doesn't break anything, no need to throw an exception
            return;
        }

        latestConnectedCP.get().switchToFastCharging();
    }

    public double getTotalCurrentConsumption(){
        double totalCurrentConsumption = getPluggedChargingPoints()
                .stream()
                .map(x -> x.isFastCharging()
                        ? Constants.FAST_CHARGING_CURRENT_AMP
                        : Constants.SLOW_CHARGING_CURRENT_AMP)
                .collect(Collectors.summingDouble(Double::doubleValue));

        if (totalCurrentConsumption > maxAvailableCurrentAmp){
            throw new UnhandledBusinessLogicException();
        }

        return totalCurrentConsumption;
    }

    private EVConnection connectToFastCharging(ChargingPoint cp, String clientId, LocalDateTime connectedAt) {
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

        return cp.plug(clientId, connectedAt, true);
    }

    private EVConnection connectToSlowCharging(ChargingPoint cp, String clientId, LocalDateTime connectedAt) {
        double currentAvailable = maxAvailableCurrentAmp - getTotalCurrentConsumption();

        // With given requirements (Number of CP = 10 and max current per park = 100) that won't run more than once
        // but if we change the values of those variables the implementation won't break (or at least not here)
        while (currentAvailable < Constants.SLOW_CHARGING_CURRENT_AMP) {
            var fastChargingCPWithOldestConnection = getPluggedChargingPoints()
                    .stream()
                    .filter(x -> x.isFastCharging())
                    .sorted(Comparator.comparing(ChargingPoint::getPluggedAt))
                    .findFirst()
                    .get();

            fastChargingCPWithOldestConnection.switchToSlowCharging();
            currentAvailable = maxAvailableCurrentAmp - getTotalCurrentConsumption();
        }

        return cp.plug(clientId, connectedAt, false);
    }

    // calculates how much of 'free' current there could be if each consumer switched to a slow charging
    private double calculateMaxPossibleCurrentAvailable() {
        var pluggedCPsCount = getPluggedChargingPoints().size();
        double currentAvailableToReserve = maxAvailableCurrentAmp - (pluggedCPsCount * Constants.SLOW_CHARGING_CURRENT_AMP);

        return currentAvailableToReserve;
    }

    private List<ChargingPoint> getAvailableChargingPoints() {
        return chargingPoints
                .stream()
                .filter(x -> !x.plugged())
                .collect(Collectors.toUnmodifiableList());
    }

    private Collection<ChargingPoint> getPluggedChargingPoints() {
        return chargingPoints
                .stream()
                .filter(x -> x.plugged())
                .collect(Collectors.toUnmodifiableList());
    }
}
