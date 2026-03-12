package com.garage.service;

import com.garage.entity.MapElement;
import com.garage.entity.MapElementType;
import com.garage.entity.ParkingSpot;
import com.garage.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParkingServiceRouteTest {

    @Test
    void shouldGenerateSimpleManhattanRoute() {
        ParkingService service = new ParkingService(
                Mockito.mock(ParkingSpotRepository.class),
                Mockito.mock(ParkingSessionRepository.class),
                Mockito.mock(BlacklistVehicleRepository.class),
                Mockito.mock(AlertRepository.class),
                Mockito.mock(PricingService.class),
                Mockito.mock(MapElementRepository.class)
        );

        var entrance = MapElement.builder().type(MapElementType.ENTRANCE).x(0).y(0).build();
        var target = ParkingSpot.builder().x(2).y(1).build();

        var route = service.generateRoute(List.of(entrance), target);

        assertEquals(4, route.size());
        assertEquals(0, route.get(0).x());
        assertEquals(0, route.get(0).y());
        assertEquals(2, route.get(route.size() - 1).x());
        assertEquals(1, route.get(route.size() - 1).y());
    }
}
