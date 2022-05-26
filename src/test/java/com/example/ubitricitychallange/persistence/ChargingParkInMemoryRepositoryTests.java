package com.example.ubitricitychallange.persistence;

import com.example.ubitricitychallange.model.ConnectEVRequest;
import com.example.ubitricitychallange.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

public class ChargingParkInMemoryRepositoryTests {
    private ChargingParkInMemoryRepository sut = new ChargingParkInMemoryRepository();

    @Test
    void findNoEntityWithGivenIdFoundShouldThrowNotFoundException() {
        // Arrange
        int parkId = 404;

        // Act & Assert
        assertThatThrownBy(() -> sut.find(parkId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(Integer.toString(parkId));
    }

    @Test
    void findExpectedEntityFound() {
        // Arrange
        int parkId = 1;

        // Act
        var result = sut.find(parkId);

        // Assert
        assertThat(result.getId()).isEqualTo(parkId);
    }

    @Test
    void findThanUpdateThanFindAgainEntityShouldBeModified() {
        // Arrange
        int parkId = 1;
        var park = sut.find(parkId);
        int cpId = 6;

        park.connect(new ConnectEVRequest("client_1", LocalDateTime.now(), cpId));
        sut.update(park);

        // Act
        var updatedPark = sut.find(parkId);

        // Assert
        assertThat(updatedPark.getId()).isEqualTo(parkId);

        var cpSix = updatedPark.getChargingPoints()
                .stream()
                .filter(x -> x.getId() == cpId)
                .findFirst()
                .get();

        assertThat(cpSix.getId()).isEqualTo(cpId);
        assertThat(cpSix.plugged()).isTrue();
    }
}
