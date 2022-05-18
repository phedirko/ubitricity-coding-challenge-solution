package com.example.ubitricitychallange.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema
public class ConnectEVRequest {
    @Schema(description = "Could be any value")
    private String clientId;

    @Schema(description = "Date and time when ev was connected. This should be sent by the client in case there is a delay or some transport layer issues")
    private LocalDateTime connectedAt;

    @Schema(description = "Id of charging point (1-10)")
    private int chargingPointId;

    public ConnectEVRequest(String clientId, LocalDateTime connectedAt, int chargingPointId) {
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
