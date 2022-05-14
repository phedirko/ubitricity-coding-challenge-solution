package com.example.ubitricitychallange.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionTests {
    @Test
    void createShouldHaveExpectedProperitiesValues(){
        // Arrange
        String clientId = "Client_1";
        boolean isFastCharging = true;
        var connectedAt = LocalDateTime.now();

        var connection = Connection.create(clientId, isFastCharging, connectedAt);

        // Act & Assert
        assertNotNull(connection.getId());
        assertNotEquals(UUID.fromString("0-0-0-0-0"), connection.getId());
        assertEquals(isFastCharging, connection.isFastCharging());
        assertEquals(connectedAt, connection.getConnectedAt());
        assertEquals(true, connection.isConnected());
    }

    @Test
    void createAndDisconnectIsConnectedShouldBeFalse(){
        // Arrange
        var connection = Connection.create("Client_2", true, LocalDateTime.now());

        // Act
        connection.disconnect(LocalDateTime.now());

        // Assert
        assertEquals(false, connection.isConnected());
    }
}
