package com.example.ubitricitychallange.services;

import com.example.ubitricitychallange.model.ChargingPoint;
import com.example.ubitricitychallange.model.EVConnection;
import com.example.ubitricitychallange.model.DisconnectEVRequest;
import com.example.ubitricitychallange.model.ConnectEVRequest;
import com.example.ubitricitychallange.persistence.ChargingParkRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class ChargingParkServiceImpl implements ChargingParkService {
    private static final Object lockObject = new Object();
    private final ChargingParkRepository repository;

    public ChargingParkServiceImpl(ChargingParkRepository repository) {
        this.repository = repository;
    }

    @Override
    public EVConnection plug(int chargingParkId, ConnectEVRequest newConnection) {
        synchronized (lockObject) {
            var chargingPark = repository.find(chargingParkId);
            var connection = chargingPark.connect(newConnection);
            repository.update(chargingPark);

            return connection;
        }
    }

    @Override
    public void unPlug(int chargeParkId, DisconnectEVRequest request) {
        synchronized (lockObject) {
            var park = repository.find(chargeParkId);
            park.disconnect(request.getDisconnectedAt(), request.getChargingPointId());
            repository.update(park);
        }
    }

    @Override
    public String getReport(int chargeParkId) {
        var park = repository.find(chargeParkId);

        var sb = new StringBuilder();
        sb.append("Report:");
        sb.append(System.lineSeparator());

        var chargingPoints = park.getChargingPoints()
                .stream()
                .sorted(Comparator.comparing(ChargingPoint::getId))
                .collect(Collectors.toList());

        for (var cp : chargingPoints) {
            if (!cp.plugged()){
                sb.append("CP" + cp.getId() + " AVAILABLE");
            } else {
                sb.append("CP" + cp.getId() + " OCCUPIED " + (cp.isFastCharging() ? "20A" : "10A"));
            }
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }
}
