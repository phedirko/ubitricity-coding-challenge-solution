package com.example.ubitricitychallange.domain;

import java.time.LocalDateTime;

public class NewEVConnection {
    private final String clientId;
    private final LocalDateTime connectedAt;
    private final int chargingPointId;

    public NewEVConnection(String clientId, LocalDateTime connectedAt, int chargingPointId) {
        this.clientId = clientId;
        this.connectedAt = connectedAt;
        this.chargingPointId = chargingPointId;
    }

    public String getClientId() {
        return clientId;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public int getChargingPointId() {
        return chargingPointId;
    }
}
