package com.garage.service;

import com.garage.entity.MapElement;
import com.garage.entity.MapElementType;
import com.garage.entity.ParkingSpot;
import com.garage.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParkingServiceRouteTest {

    @Test
    void shouldGenerateAStarRouteAvoidingObstacle() {
        ParkingService service = new ParkingService(
                Mockito.mock(ParkingSpotRepository.class),
                Mockito.mock(ParkingSessionRepository.class),
                Mockito.mock(BlacklistVehicleRepository.class),
                Mockito.mock(AlertRepository.class),
                Mockito.mock(PricingService.class),
                Mockito.mock(MapElementRepository.class)
        );

        var entrance = MapElement.builder().type(MapElementType.ENTRANCE).x(0).y(0).build();
        var obstacle = MapElement.builder().type(MapElementType.PILLAR).x(1).y(0).width(1).height(1).build();
        var target = ParkingSpot.builder().id(99L).x(2).y(0).occupied(false).build();

        var route = service.generateRoute(List.of(entrance, obstacle), List.of(target), target);

        assertFalse(route.isEmpty());
        assertEquals(0, route.get(0).x());
        assertEquals(0, route.get(0).y());
        assertEquals(2, route.get(route.size() - 1).x());
        assertEquals(0, route.get(route.size() - 1).y());
        assertFalse(route.stream().anyMatch(p -> p.x() == 1 && p.y() == 0));
    }
}
