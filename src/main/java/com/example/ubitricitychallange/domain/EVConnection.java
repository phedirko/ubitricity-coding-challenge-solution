package com.example.ubitricitychallange.domain;

import java.time.LocalDateTime;

// todo: refactor to the CP entity
public class EVConnection {
    private int id;
    private int evId;
    private LocalDateTime connectedAt;
    private LocalDateTime disconnectedAt;
    private boolean isFastCharging;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEvId() {
        return evId;
    }

    public void setEvId(int evId) {
        this.evId = evId;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(LocalDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }

    public LocalDateTime getDisconnectedAt() {
        return disconnectedAt;
    }

    public void setDisconnectedAt(LocalDateTime disconnectedAt) {
        this.disconnectedAt = disconnectedAt;
    }

    public boolean isChargingNow() {
        return disconnectedAt == null;
    }


    // todo: this should be a part of ChargingPoint
    public boolean isFastCharging() {
        return isFastCharging;
    }

    public void setFastCharging(boolean fastCharging) {
        this.isFastCharging = fastCharging;
    }
}
