package com.example.ubitricitychallange.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema
public class DisconnectEVRequest {
    public DisconnectEVRequest(int chargingPointId, LocalDateTime disconnectedAt) {
        this.chargingPointId = chargingPointId;
        this.disconnectedAt = disconnectedAt;
    }

    @Schema(description = "id of charging point (1-10)")
    private int chargingPointId;

    @Schema(description = "Date and time when EV was disconnected. Should be sent by client.")
    private LocalDateTime disconnectedAt;
    public int getChargingPointId() {
        return chargingPointId;
    }
    public LocalDateTime getDisconnectedAt() {
        return disconnectedAt;
    }
}
