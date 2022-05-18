package com.example.ubitricitychallange.model;

import com.example.ubitricitychallange.exceptions.AlreadyPluggedException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class ChargingPoint {
    public ChargingPoint(int id) {
        this.id = id;
        this.connections = new HashSet<>();
    }
    private int id;
    private EVConnection activeConnection;
    private Set<EVConnection> connections;

    public int getId() {
        return id;
    }

    public boolean isFastCharging() {
        return activeConnection.isFastCharging();
    }

    public boolean plugged() {
        return activeConnection != null;
    }

    public EVConnection plug(String clientId, LocalDateTime connectedAt, boolean fastCharging) {
        if (plugged()){
            throw new AlreadyPluggedException(getId());
        }

        var connection= EVConnection.create(clientId, fastCharging, connectedAt);
        connections.add(connection);
        activeConnection = connection;

        return connection;
    }

    public void unplug(LocalDateTime disconnectedAt) {
        activeConnection.disconnect(disconnectedAt);
        activeConnection = null;
    }

    public void switchToSlowCharging() {
        if (!isFastCharging()) {
            throw new RuntimeException("Already switched to slow charging");
        }

        var currentConnection = activeConnection;
        var now = LocalDateTime.now();

        unplug(now);
        plug(currentConnection.getClientId(), now, false);
    }

    public void switchToFastCharging(){
        if (isFastCharging()) {
            throw new RuntimeException("Already switched to fast charging");
        }

        var currentConnection = activeConnection;
        var now = LocalDateTime.now();

        unplug(now);
        plug(currentConnection.getClientId(), now, true);
    }

    public LocalDateTime getPluggedAt() {
        return activeConnection.getConnectedAt();
    }

    public Set<EVConnection> getConnections() {
        return connections;
    }
}
