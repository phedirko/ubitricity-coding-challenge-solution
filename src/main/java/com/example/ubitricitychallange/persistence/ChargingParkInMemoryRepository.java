package com.example.ubitricitychallange.persistence;

import com.example.ubitricitychallange.model.ChargingPark;
import com.example.ubitricitychallange.exceptions.NotFoundException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Component
public class ChargingParkInMemoryRepository implements ChargingParkRepository {
    private final HashMap<Integer, ChargingPark> storage = new HashMap<Integer, ChargingPark>();

    public ChargingParkInMemoryRepository() {
        storage.put(1, ChargingPark.createDefault());
    }

    @Override
    public ChargingPark find(int id) {
        var cp = storage.get(id);

        if (cp == null) {
            throw new NotFoundException(ChargingPark.class.getName(), id);
        }

        return cp;
    }

    @Override
    public void update(ChargingPark chargingPark) {
        storage.put(chargingPark.getId(), chargingPark);
    }
}
