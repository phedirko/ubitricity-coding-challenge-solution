package com.example.ubitricitychallange.persistence;

import com.example.ubitricitychallange.model.ChargingPark;

public interface ChargingParkRepository {
    ChargingPark find(int id);
    void update(ChargingPark chargingPark);
}
