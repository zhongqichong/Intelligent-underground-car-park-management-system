package com.garage.service;

import com.garage.entity.Alert;
import com.garage.entity.ParkingSession;
import com.garage.entity.ParkingSpot;
import com.garage.repository.AlertRepository;
import com.garage.repository.ParkingSessionRepository;
import com.garage.repository.ParkingSpotRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    @Test
    void shouldBuildOverviewMetrics() {
        ParkingSpotRepository spotRepository = Mockito.mock(ParkingSpotRepository.class);
        ParkingSessionRepository sessionRepository = Mockito.mock(ParkingSessionRepository.class);
        AlertRepository alertRepository = Mockito.mock(AlertRepository.class);

        ParkingSpot occupied = ParkingSpot.builder().occupied(true).build();
        ParkingSpot free = ParkingSpot.builder().occupied(false).build();

        ParkingSession todayPaid = ParkingSession.builder()
                .entryTime(LocalDateTime.now())
                .exitTime(LocalDateTime.now())
                .fee(BigDecimal.valueOf(12))
                .active(false)
                .build();
        ParkingSession activeSession = ParkingSession.builder()
                .entryTime(LocalDateTime.now())
                .active(true)
                .build();

        Alert unresolved = Alert.builder().resolved(false).build();

        when(spotRepository.findAll()).thenReturn(List.of(occupied, free));
        when(sessionRepository.findAll()).thenReturn(List.of(todayPaid, activeSession));
        when(alertRepository.findAll()).thenReturn(List.of(unresolved));

        ReportService service = new ReportService(spotRepository, sessionRepository, alertRepository);
        var result = service.overview();

        assertEquals(2, result.totalSpots());
        assertEquals(1, result.occupiedSpots());
        assertEquals(1, result.freeSpots());
        assertEquals(1, result.activeSessions());
        assertEquals(BigDecimal.valueOf(12), result.todayRevenue());
        assertEquals(2, result.todayEntries());
        assertEquals(1, result.unresolvedAlerts());
        assertEquals(7, result.recentDailyRevenue().size());
    }
}
