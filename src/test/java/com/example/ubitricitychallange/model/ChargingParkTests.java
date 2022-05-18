package com.example.ubitricitychallange.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;

public class ChargingParkTests {
    @Test
    void cpIdNotFoundShouldThrowException(){
        // Arrange
        var sut = CreateChargingPark();
        var newConnection = new ConnectEVRequest("Client_1", LocalDateTime.now(), 1201);

        // Act & Assert
        Assertions.assertThrows(RuntimeException.class, () -> sut.connect(newConnection));
    }

    @Test
    void noCarsConnectedShouldConnectToFastCharging(){
        // Arrange
        var sut = CreateChargingPark();
        // Act
        var connection = sut.connect(new ConnectEVRequest("Client_11", LocalDateTime.now(), 3));

        // Assert
        Assertions.assertTrue(connection.isConnected());
        Assertions.assertTrue(connection.isFastCharging());
    }

    @Test
    void connectFourCarsThenOneMoreShouldBeConnectedToFastCharging(){
        // Arrange
        var sut = CreateChargingPark();

        for(int i = 1; i <= 4; i++){
            sut.connect(new ConnectEVRequest(String.format("Client_%s", i), LocalDateTime.now(), i));
        }

        // Act
        var connection = sut.connect(new ConnectEVRequest("Client_12", LocalDateTime.now(), 8));

        // Assert
        Assertions.assertTrue(connection.isConnected());
        Assertions.assertTrue(connection.isFastCharging());
    }

    @Test
    void connectFiveCarsThenConnectOneMoreFirstConnectedShouldBeSwitchedToSlowCharging(){
        // Arrange
        var sut = CreateChargingPark();

        int firstConnectionCPId = 1;
        var firstConnection = sut.connect(new ConnectEVRequest(String.format("Client_%s", 1),
                                                     LocalDateTime.now().minusSeconds(5),
                                                     firstConnectionCPId));

        sut.connect(new ConnectEVRequest(String.format("Client_%s", 2), LocalDateTime.now(), 2));
        sut.connect(new ConnectEVRequest(String.format("Client_%s", 3), LocalDateTime.now(), 3));
        sut.connect(new ConnectEVRequest(String.format("Client_%s", 4), LocalDateTime.now(), 4));
        sut.connect(new ConnectEVRequest(String.format("Client_%s", 5), LocalDateTime.now(), 5));

        // Act
        var newConnection = sut.connect(new ConnectEVRequest("Client_6", LocalDateTime.now(), 8));

        // Assert
        var chargingPointOne = sut.getChargingPoints().stream().filter(x -> x.getId() == firstConnectionCPId).findFirst();

        Assertions.assertTrue(newConnection.isConnected());
        Assertions.assertTrue(newConnection.isFastCharging());
        Assertions.assertFalse(chargingPointOne.get().isFastCharging());
        Assertions.assertTrue(chargingPointOne.get().plugged());
    }

    @Test
    void connectEightCarsThenOneMoreShouldBeConnectedToFastCharging(){
        // Arrange
        var sut = CreateChargingPark();

        for(int i = 1; i <= 8; i++){
            sut.connect(new ConnectEVRequest(String.format("Client_%s", i), LocalDateTime.now(), i));
        }

        // Act
        var connection = sut.connect(new ConnectEVRequest("Client_13", LocalDateTime.now(), 9));

        // Assert
        Assertions.assertTrue(connection.isConnected());
        Assertions.assertTrue(connection.isFastCharging());
    }

    @Test
    void connectNineCarsThenOneMoreShouldBeConnectedToSlowCharging(){
        // Arrange
        var sut = CreateChargingPark();

        for(int i = 1; i <= 9; i++){
            sut.connect(new ConnectEVRequest(String.format("Client_%s", i), LocalDateTime.now(), i));
        }

        // Act
        var connection = sut.connect(new ConnectEVRequest("Client_14", LocalDateTime.now(), 10));

        // Assert
        Assertions.assertTrue(connection.isConnected());
        Assertions.assertFalse(connection.isFastCharging());
    }

    @Test
    void connectTenCarsThenDisconnectOneByOneAndCheckTotalConsumption(){
        // Arrange
        var sut = CreateChargingPark();

        for(int i = 1; i <= 10; i++){
            sut.connect(new ConnectEVRequest(String.format("Client_%s", 1), LocalDateTime.now(), i));
        }

        // Act & Assert
        sut.disconnect(LocalDateTime.now(), 1);
        Assertions.assertEquals(Constants.MAX_AVAILABLE_CURRENT_AMP_PER_CHARGING_PARK, sut.getTotalCurrentConsumption());

        sut.disconnect(LocalDateTime.now(), 2);
        Assertions.assertEquals(Constants.MAX_AVAILABLE_CURRENT_AMP_PER_CHARGING_PARK, sut.getTotalCurrentConsumption());

        sut.disconnect(LocalDateTime.now(), 3);
        Assertions.assertEquals(Constants.MAX_AVAILABLE_CURRENT_AMP_PER_CHARGING_PARK, sut.getTotalCurrentConsumption());

        sut.disconnect(LocalDateTime.now(), 4);
        Assertions.assertEquals(Constants.MAX_AVAILABLE_CURRENT_AMP_PER_CHARGING_PARK, sut.getTotalCurrentConsumption());

        sut.disconnect(LocalDateTime.now(), 5);
        Assertions.assertEquals(Constants.MAX_AVAILABLE_CURRENT_AMP_PER_CHARGING_PARK, sut.getTotalCurrentConsumption());

        sut.disconnect(LocalDateTime.now(), 6);
        Assertions.assertEquals(Constants.FAST_CHARGING_CURRENT_AMP * 4, sut.getTotalCurrentConsumption());

        sut.disconnect(LocalDateTime.now(), 7);
        Assertions.assertEquals(Constants.FAST_CHARGING_CURRENT_AMP * 3, sut.getTotalCurrentConsumption());

        sut.disconnect(LocalDateTime.now(), 8);
        Assertions.assertEquals(Constants.FAST_CHARGING_CURRENT_AMP * 2, sut.getTotalCurrentConsumption());

        sut.disconnect(LocalDateTime.now(), 9);
        Assertions.assertEquals(Constants.FAST_CHARGING_CURRENT_AMP * 1, sut.getTotalCurrentConsumption());

        sut.disconnect(LocalDateTime.now(), 10);
        Assertions.assertEquals(0, sut.getTotalCurrentConsumption());
    }

    @Test
    void connectTenCarsThenDisconnectOneTheLatestConnectedShouldBeSwitchedToFastCharging() {
        // Arrange
        var sut = CreateChargingPark();

        for(int i = 1; i <= 9; i++){
            sut.connect(new ConnectEVRequest(String.format("Client_%s", i), LocalDateTime.now(), i));
        }

        // making these connections 'older' so the CP would be 100% the latest connected
        sut.connect(
                new ConnectEVRequest(String.format("Client_%s", 10), LocalDateTime.now().plusSeconds(10),10));

        // Act
        sut.disconnect(LocalDateTime.now(), 5);

        // Assert
        var latestConnectedPlug = sut.getChargingPoints().stream().filter(x -> x.getId() == 10).findFirst();
        Assertions.assertTrue(latestConnectedPlug.get().isFastCharging());
    }

    private static ChargingPark CreateChargingPark(){
        var chargingPointsSet = new HashSet<ChargingPoint>();
        chargingPointsSet.add(new ChargingPoint(1));
        chargingPointsSet.add(new ChargingPoint(2));
        chargingPointsSet.add(new ChargingPoint(3));
        chargingPointsSet.add(new ChargingPoint(4));
        chargingPointsSet.add(new ChargingPoint(5));
        chargingPointsSet.add(new ChargingPoint(6));
        chargingPointsSet.add(new ChargingPoint(7));
        chargingPointsSet.add(new ChargingPoint(8));
        chargingPointsSet.add(new ChargingPoint(9));
        chargingPointsSet.add(new ChargingPoint(10));

        return new ChargingPark(1, chargingPointsSet, Constants.MAX_AVAILABLE_CURRENT_AMP_PER_CHARGING_PARK);
    }

    // 1. Target CP not found - throws exception - DONE
    // 2. No cars connected - plug to a fast charge - DONE
    // 3. 4 cars connected - plug to a fast charge - DONE
    // 4. 8 cars connected - plug to a fast charge - DONE
    // 5. 9 cars connected - plug to a slow charge - DONE
    // 6. 10 cars connected, unplug one, the latest connected switches to a fast charge
    // 7. 10 cars connected, unplug one by one, check FastCharge and total consumption - DONE
}
