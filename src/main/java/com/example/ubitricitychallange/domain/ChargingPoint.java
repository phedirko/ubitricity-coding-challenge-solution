package com.example.ubitricitychallange.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class ChargingPoint {
    public ChargingPoint(int id) {
        this.id = id;
        this.connections = new HashSet<Connection>();
    }
    private int id;
    private Connection activeConnection;
    private Set<Connection> connections;

    public int getId() {
        return id;
    }

    public boolean isFastCharging() {
        return activeConnection.isFastCharging();
    }

    public boolean EVPlugged() {
        return activeConnection != null;
    }

    public Connection connect(String clientId, LocalDateTime connectedAt, boolean fastCharging) { // todo: connection request
        if (EVPlugged()){
            throw new RuntimeException("The CP is already plugged"); // TODO: type for an exception
        }

        var connection= Connection.create(clientId, fastCharging, connectedAt); // todo: return connection object
        connections.add(connection);
        activeConnection = connection;

        return connection;
    }

    public void disconnect(LocalDateTime disconnectedAt) {
        activeConnection.disconnect(disconnectedAt);
        activeConnection = null;
    }

    public void switchToSlowCharging() {
        if (!isFastCharging()) {
            throw new RuntimeException("Already switched to slow charging");
        }

        var currentConnection = activeConnection;
        var now = LocalDateTime.now();

        disconnect(now);
        connect(currentConnection.getClientId(), now, false);
    }

    public void switchToFastCharging(){
        if (!isFastCharging()) {
            throw new RuntimeException("Already switched to fast charging");
        }

        var currentConnection = activeConnection;
        var now = LocalDateTime.now();

        disconnect(now);
        connect(currentConnection.getClientId(), now, true);
    }

    public LocalDateTime getPluggedAt() {
        return activeConnection.getConnectedAt();
    }

    public Set<Connection> getConnections() {
        return connections;
    }
}
