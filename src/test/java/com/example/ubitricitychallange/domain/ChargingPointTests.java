package com.example.ubitricitychallange.domain;

import org.apache.tomcat.jni.Local;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ChargingPointTests {
    @Test
    void createChargingPointIdShouldBeAsPassedConnectionsShouldBeEmpty(){
        // Arrange
        int cpId = 1;
        var cp = new ChargingPoint(cpId);

        // Act & Assert
        assertEquals(cpId, cp.getId());
        assertTrue(cp.getConnections().isEmpty());
    }

    @Test
    void connectToChargingPointAsFastChargingPropertiesShouldHaveExpectedValues(){
        // Arrange
        var cp = createCP();
        String clientId = "Client_1101";

        // Act
        cp.connect(clientId, LocalDateTime.now(), true);

        // Assert
        var connection = cp.getConnections().iterator().next();

        assertTrue(cp.EVPlugged());
        assertTrue(cp.isFastCharging());
        assertEquals(clientId, connection.getClientId());
        assertTrue(connection.isConnected());
    }

    @Test
    void connectAndThenDisconnectShouldBeDisconnected(){
        // Arrange
        var cp = createCP();
        String clientId = "Client_1102";

        // Act
        cp.connect(clientId, LocalDateTime.now(), true);
        cp.disconnect(LocalDateTime.now());

        // Assert
        var connections = cp.getConnections();
        assertFalse(cp.EVPlugged());
        assertEquals(1, connections.size());
        assertTrue(connections.stream().allMatch(x -> x.getClientId() == clientId));
    }

    private static ChargingPoint createCP(){
        return new ChargingPoint(1001);
    }
}
