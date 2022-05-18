package com.example.ubitricitychallange.model;

import com.example.ubitricitychallange.exceptions.AlreadyPluggedException;
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
    void plugToChargingPointAsFastChargingPropertiesShouldHaveExpectedValues(){
        // Arrange
        var cp = createCP();
        String clientId = "Client_1101";

        // Act
        cp.plug(clientId, LocalDateTime.now(), true);

        // Assert
        var connection = cp.getConnections().iterator().next();

        assertTrue(cp.plugged());
        assertTrue(cp.isFastCharging());
        assertEquals(clientId, connection.getClientId());
        assertTrue(connection.isConnected());
    }

    @Test
    void plugAndThenDisconnectShouldBeDisconnected(){
        // Arrange
        var cp = createCP();
        String clientId = "Client_1102";

        // Act
        cp.plug(clientId, LocalDateTime.now(), true);
        cp.unplug(LocalDateTime.now());

        // Assert
        var connections = cp.getConnections();
        assertFalse(cp.plugged());
        assertEquals(1, connections.size());
        assertTrue(connections.stream().allMatch(x -> x.getClientId() == clientId));
    }

    @Test
    void plugAndThenSwitchToSlowChargingShouldHaveTwoConnections(){
        // Arrange
        var cp = createCP();
        String clientId = "Client_1103";

        // Act
        cp.plug(clientId, LocalDateTime.now(), true);
        cp.switchToSlowCharging();

        // Assert
        var connections = cp.getConnections();
        var slowConnection = connections.stream().filter(x -> !x.isFastCharging()).findFirst();
        var fastConnection = connections.stream().filter(x -> x.isFastCharging()).findFirst();

        assertTrue(cp.plugged());
        assertFalse(cp.isFastCharging());
        assertEquals(2, connections.size());
        assertTrue(connections.stream().allMatch(x -> x.getClientId() == clientId));
        assertNull(slowConnection.get().getDisconnectedAt());
        assertFalse(slowConnection.get().isFastCharging());
        assertFalse(fastConnection.get().isConnected());
    }

    @Test
    void plugAsFastChargingAndSwitchToFastChargingShouldThrowException(){
        // Arrange
        var cp = createCP();
        String clientId = "Client_1104";

        // Act
        cp.plug(clientId, LocalDateTime.now(), true);

        // Assert
        Assertions.assertThrows(RuntimeException.class, () -> {
            cp.switchToFastCharging();
        });
    }

    @Test
    void plugAsSlowChargingAndSwitchToSlowChargingShouldThrowException(){
        // Arrange
        var cp = createCP();
        String clientId = "Client_1105";

        // Act
        cp.plug(clientId, LocalDateTime.now(), false);

        // Assert
        Assertions.assertThrows(RuntimeException.class, () -> {
            cp.switchToSlowCharging();
        });
    }

    @Test
    void plugAsFastChargingSwitchToSlowThenSwitchToFastShouldHaveThreeConnections() {
        // Arrange
        var cp = createCP();
        String clientId = "Client_1106";

        // Act
        cp.plug(clientId, LocalDateTime.now(), true);
        cp.switchToSlowCharging();
        cp.switchToFastCharging();

        // Assert
        var connections = cp.getConnections();
        assertTrue(cp.plugged());
        assertEquals(3, connections.size());
        assertTrue(cp.isFastCharging());
        assertTrue(connections.stream().allMatch(x -> x.getClientId() == clientId));
    }

    @Test
    void plugThenPlugAgainShouldThrowAlreadyPluggedException() {
        // Arrange
        var cp = createCP();

        cp.plug("client1", LocalDateTime.now(), true);

        // Act & Assert
        Assertions.assertThrows(AlreadyPluggedException.class, () -> {
            cp.plug("client2", LocalDateTime.now(), true);
        });
    }

    private static ChargingPoint createCP(){
        return new ChargingPoint(1001);
    }
}
