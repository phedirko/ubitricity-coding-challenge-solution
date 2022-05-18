package com.example.ubitricitychallange.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class EVConnection {
    public EVConnection(UUID id,
                        String clientId,
                        boolean isFastCharging,
                        LocalDateTime connectedAt) {
        this.id = id;
        this.clientId = clientId;
        this.isFastCharging = isFastCharging;
        this.connectedAt = connectedAt;
    }

    public static EVConnection create(String clientId, boolean isFastCharging, LocalDateTime connectedAt)
    {
        return new EVConnection(UUID.randomUUID(), clientId, isFastCharging, connectedAt);
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
