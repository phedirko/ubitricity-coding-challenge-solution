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

    public boolean isOccupied() {
        return activeConnection != null;
    }

    public void connect(String clientId, LocalDateTime connectedAt, boolean fastCharging) {
        var connection= Connection.Create(clientId, fastCharging, connectedAt); // todo: return connection object
        connections.add(connection);
        activeConnection = connection;
    }

    public void disconnect(LocalDateTime disconnectedAt) {
        activeConnection.disconnect(disconnectedAt);
        activeConnection = null;
    }

}
