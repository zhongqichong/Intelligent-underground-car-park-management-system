package com.garage.service;

import com.garage.dto.DashboardOverviewResponse;
import com.garage.repository.AlertRepository;
import com.garage.repository.ParkingSessionRepository;
import com.garage.repository.ParkingSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ParkingSpotRepository parkingSpotRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final AlertRepository alertRepository;

    public DashboardOverviewResponse overview() {
        var spots = parkingSpotRepository.findAll();
        var sessions = parkingSessionRepository.findAll();
        var alerts = alertRepository.findAll();

        long totalSpots = spots.size();
        long occupiedSpots = spots.stream().filter(s -> Boolean.TRUE.equals(s.getOccupied())).count();
        long freeSpots = totalSpots - occupiedSpots;
        long activeSessions = sessions.stream().filter(s -> Boolean.TRUE.equals(s.getActive())).count();

        LocalDate today = LocalDate.now();
        BigDecimal todayRevenue = sessions.stream()
                .filter(s -> s.getExitTime() != null && s.getExitTime().toLocalDate().isEqual(today))
                .filter(s -> s.getFee() != null)
                .map(s -> s.getFee())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long todayEntries = sessions.stream()
                .filter(s -> s.getEntryTime() != null && s.getEntryTime().toLocalDate().isEqual(today))
                .count();

        long unresolvedAlerts = alerts.stream().filter(a -> !Boolean.TRUE.equals(a.getResolved())).count();

        List<DashboardOverviewResponse.DailyRevenuePoint> recent = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            BigDecimal dayRevenue = sessions.stream()
                    .filter(s -> s.getExitTime() != null && s.getExitTime().toLocalDate().isEqual(day))
                    .filter(s -> s.getFee() != null)
                    .map(s -> s.getFee())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            recent.add(new DashboardOverviewResponse.DailyRevenuePoint(day.toString(), dayRevenue));
        }

        return new DashboardOverviewResponse(totalSpots, occupiedSpots, freeSpots, activeSessions,
                todayRevenue, todayEntries, unresolvedAlerts, recent);
    }
}
