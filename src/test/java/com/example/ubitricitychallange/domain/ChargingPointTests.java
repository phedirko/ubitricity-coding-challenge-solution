package com.example.ubitricitychallange.domain;

import org.junit.jupiter.api.Assertions;
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

    @Test
    void connectAndThenSwitchToSlowChargingShouldHaveTwoConnections(){
        // Arrange
        var cp = createCP();
        String clientId = "Client_1103";

        // Act
        cp.connect(clientId, LocalDateTime.now(), true);
        cp.switchToSlowCharging();

        // Assert
        var connections = cp.getConnections();
        var slowConnection = connections.stream().filter(x -> !x.isFastCharging()).findFirst();
        var fastConnection = connections.stream().filter(x -> x.isFastCharging()).findFirst();

        assertTrue(cp.EVPlugged());
        assertFalse(cp.isFastCharging());
        assertEquals(2, connections.size());
        assertTrue(connections.stream().allMatch(x -> x.getClientId() == clientId));
        assertNull(slowConnection.get().getDisconnectedAt());
        assertFalse(slowConnection.get().isFastCharging());
        assertFalse(fastConnection.get().isConnected());
    }

    @Test
    void connectAsFastChargingAndSwitchToFastChargingShouldThrowException(){
        // Arrange
        var cp = createCP();
        String clientId = "Client_1104";

        // Act
        cp.connect(clientId, LocalDateTime.now(), true);

        // Assert
        Assertions.assertThrows(RuntimeException.class, () -> {
            cp.switchToFastCharging();
        });
    }

    @Test
    void connectAsSlowChargingAndSwitchToSlowChargingShouldThrowException(){
        // Arrange
        var cp = createCP();
        String clientId = "Client_1105";

        // Act
        cp.connect(clientId, LocalDateTime.now(), false);

        // Assert
        Assertions.assertThrows(RuntimeException.class, () -> {
            cp.switchToSlowCharging();
        });
    }

    @Test
    void connectAsFastChargingSwitchToSlowThenSwitchToFastShouldHaveThreeConnections(){
        // Arrange
        var cp = createCP();
        String clientId = "Client_1106";

        // Act
        cp.connect(clientId, LocalDateTime.now(), true);
        cp.switchToSlowCharging();
        cp.switchToFastCharging();

        // Assert
        var connections = cp.getConnections();
        assertTrue(cp.EVPlugged());
        assertEquals(3, connections.size());
        assertTrue(cp.isFastCharging());
        assertTrue(connections.stream().allMatch(x -> x.getClientId() == clientId));
    }

    private static ChargingPoint createCP(){
        return new ChargingPoint(1001);
    }
}
