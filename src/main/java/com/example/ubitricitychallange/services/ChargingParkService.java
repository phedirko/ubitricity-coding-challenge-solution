package com.example.ubitricitychallange.services;

import com.example.ubitricitychallange.model.EVConnection;
import com.example.ubitricitychallange.model.DisconnectEVRequest;
import com.example.ubitricitychallange.model.ConnectEVRequest;

public interface ChargingParkService {
    EVConnection plug(int chargingParkId, ConnectEVRequest newConnection);
    void unPlug(int chargeParkId, DisconnectEVRequest request);
    String getReport(int chargeParkId);
}