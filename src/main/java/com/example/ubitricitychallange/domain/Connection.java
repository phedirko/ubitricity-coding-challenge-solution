package com.example.ubitricitychallange.domain;

import java.time.LocalDateTime;
import java.util.UUID;

// todo: rename to EV Connection?
public class Connection {
    public Connection(UUID id,
                      String clientId,
                      boolean isFastCharging,
                      LocalDateTime connectedAt) {
        this.id = id;
        this.clientId = clientId;
        this.isFastCharging = isFastCharging;
        this.connectedAt = connectedAt;
    }

    public static Connection Create(String clientId, boolean isFastCharging, LocalDateTime connectedAt)
    {
        return new Connection(UUID.randomUUID(), clientId, isFastCharging, connectedAt);
    }

    private UUID id;
    private String clientId; // Could be any string
    private boolean isFastCharging;
    private LocalDateTime connectedAt;
    private LocalDateTime disconnectedAt;

    public UUID getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public LocalDateTime getDisconnectedAt() {
        return disconnectedAt;
    }

    public boolean isConnected() {
        return disconnectedAt == null;
    }

    public boolean isFastCharging() {
        return isFastCharging;
    }

    public void disconnect(LocalDateTime disconnectedAt) {
        this.disconnectedAt = disconnectedAt;
    }
}
