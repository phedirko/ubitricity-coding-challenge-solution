package com.example.ubitricitychallange.services;

import com.example.ubitricitychallange.model.ChargingPark;
import com.example.ubitricitychallange.model.ConnectEVRequest;
import com.example.ubitricitychallange.model.DisconnectEVRequest;
import com.example.ubitricitychallange.exceptions.NotFoundException;
import com.example.ubitricitychallange.persistence.ChargingParkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class ChargingParkServiceImplTests {
    private ChargingParkRepository chargingParkRepoMock;
    private ChargingParkServiceImpl sut;

    @BeforeEach
    void initSut(){
        chargingParkRepoMock = Mockito.mock(ChargingParkRepository.class);
        sut = new ChargingParkServiceImpl(chargingParkRepoMock);
    }

    @Test
    void plugShouldReturnNewConnectionAndCallRepoUpdate() {
        // Arrange
        int cpId = 1;
        var request = new ConnectEVRequest("client_501", LocalDateTime.now(), 2);
        when(chargingParkRepoMock.find(1)).thenReturn(ChargingPark.createDefault());

        // Act
        var newConnection = sut.plug(cpId, request);

        // Assert
        assertThat(newConnection.isConnected()).isTrue();
        verify(chargingParkRepoMock, times(1)).find(cpId);
        verify(chargingParkRepoMock, times(1)).update(any(ChargingPark.class));
    }

    @Test
    void plugRepoThrowsNotFoundExceptionShouldAlsoThrowNotFoundException() {
        // Arrange
        int parkId = 404;

        when(chargingParkRepoMock.find(parkId))
                .thenThrow(new NotFoundException(ChargingPark.class.getName(), parkId));

        // Act & Assert
        assertThatThrownBy(
                () -> sut.plug(parkId,
                        new ConnectEVRequest("client2", LocalDateTime.now(), 2)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void unPlugShouldCallRepoFindAndUpdate() {
        // Arrange
        int parkId = 4;
        int cpId = 3;
        var request = new DisconnectEVRequest(cpId, LocalDateTime.now());
        when(chargingParkRepoMock.find(parkId)).thenReturn(ChargingPark.createDefault());

        // Act
        sut.plug(parkId, new ConnectEVRequest("James_Bond", LocalDateTime.now(), cpId));
        sut.unPlug(parkId, request);

        // Assert
        verify(chargingParkRepoMock, times(2)).find(parkId);
        verify(chargingParkRepoMock, times(2)).update(any(ChargingPark.class));
    }

    @Test
    void unPlugRepoThrowsNotFoundExceptionShouldThrowNotFoundException() {
        // Arrange
        int parkId = 10404;

        when(chargingParkRepoMock.find(parkId))
                .thenThrow(new NotFoundException(ChargingPark.class.getName(), parkId));

        // Act & Assert
        assertThatThrownBy(
                () -> sut.plug(parkId,
                        new ConnectEVRequest("client18", LocalDateTime.now(), 2)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getReportShouldAllBeAvailable() {
        // Arrange
        var chargingPark = ChargingPark.createDefault();
        when(chargingParkRepoMock.find(chargingPark.getId()))
                .thenReturn(chargingPark);
        // Act
        String report = sut.getReport(chargingPark.getId());

        // Assert
        assertThat(report).doesNotContain("OCCUPIED");
    }

    @Test
    void getReportPlugEVOnceOneCPShouldBeOccupied() {
        // Arrange
        var chargingPark = ChargingPark.createDefault();
        int cpId = 8;
        chargingPark.connect(new ConnectEVRequest("Jason_Bourne", LocalDateTime.now(), cpId));
        when(chargingParkRepoMock.find(chargingPark.getId()))
                .thenReturn(chargingPark);

        // Act
        String report = sut.getReport(chargingPark.getId());

        // Assert
        assertThat(report).contains("CP" + 8 + " OCCUPIED 20A");
    }
}
