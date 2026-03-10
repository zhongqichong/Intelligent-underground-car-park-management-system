package com.garage.service;

import com.garage.dto.EntryRequest;
import com.garage.dto.ExitResponse;
import com.garage.dto.MapOverviewResponse;
import com.garage.dto.SpotRecommendationResponse;
import com.garage.entity.Alert;
import com.garage.entity.MapElementType;
import com.garage.entity.ParkingSession;
import com.garage.entity.ParkingSpot;
import com.garage.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingService {
    private final ParkingSpotRepository spotRepository;
    private final ParkingSessionRepository sessionRepository;
    private final BlacklistVehicleRepository blacklistVehicleRepository;
    private final AlertRepository alertRepository;
    private final PricingService pricingService;
    private final MapElementRepository mapElementRepository;

    public SpotRecommendationResponse recommend() {
        ParkingSpot best = spotRepository.findByOccupiedFalse().stream()
                .min(Comparator.comparingDouble(this::score))
                .orElseThrow(() -> new IllegalStateException("No free spots"));
        return new SpotRecommendationResponse(best.getCode(), score(best), "Nearest elevator + lower congestion");
    }

    public MapOverviewResponse mapOverview() {
        var spots = spotRepository.findAll();
        var elements = mapElementRepository.findAll();
        var recommendation = recommend();
        var recommendedSpot = spots.stream().filter(s -> recommendation.spotCode().equals(s.getCode())).findFirst().orElseThrow();
        var route = generateRoute(elements, recommendedSpot);
        return new MapOverviewResponse(40, 25, spots, elements, recommendation, route);
    }

    List<MapOverviewResponse.RoutePoint> generateRoute(List<com.garage.entity.MapElement> elements, ParkingSpot targetSpot) {
        var entrance = elements.stream()
                .filter(e -> e.getType() == MapElementType.ENTRANCE)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No entrance configured"));

        int x = entrance.getX();
        int y = entrance.getY();
        int tx = targetSpot.getX();
        int ty = targetSpot.getY();

        List<MapOverviewResponse.RoutePoint> path = new ArrayList<>();
        path.add(new MapOverviewResponse.RoutePoint(x, y));

        while (x != tx) {
            x += x < tx ? 1 : -1;
            path.add(new MapOverviewResponse.RoutePoint(x, y));
        }
        while (y != ty) {
            y += y < ty ? 1 : -1;
            path.add(new MapOverviewResponse.RoutePoint(x, y));
        }

        return path;
    }

    private double score(ParkingSpot s) {
        double elevatorDistance = s.getNearElevator() ? 1 : 5;
        double congestionCost = s.getZoneLoad() * 1.5;
        double pillarPenalty = s.getNearPillar() ? 4 : 0;
        return elevatorDistance + congestionCost + pillarPenalty;
    }

    @Transactional
    public ParkingSession entry(EntryRequest request) {
        sessionRepository.findByPlateNumberAndActiveTrue(request.plateNumber()).ifPresent(s -> {
            throw new IllegalArgumentException("Vehicle already in garage");
        });
        ParkingSpot spot = spotRepository.findByCode(recommend().spotCode());
        if (!sessionRepository.findBySpotIdAndActiveTrue(spot.getId()).isEmpty()) {
            createAlert("SPOT_CONFLICT", request.plateNumber(), "Spot conflict detected: duplicated occupation");
        }
        if (blacklistVehicleRepository.existsByPlateNumber(request.plateNumber())) {
            createAlert("BLACKLIST_ENTRY", request.plateNumber(), "Blacklisted vehicle entry attempt");
        }
        spot.setOccupied(true);
        spotRepository.save(spot);

        ParkingSession session = ParkingSession.builder()
                .plateNumber(request.plateNumber())
                .spot(spot)
                .entryTime(LocalDateTime.now())
                .paymentStatus("UNPAID")
                .active(true)
                .build();
        return sessionRepository.save(session);
    }

    @Transactional
    public ExitResponse exit(String plateNumber) {
        ParkingSession session = sessionRepository.findByPlateNumberAndActiveTrue(plateNumber)
                .orElseThrow(() -> new IllegalArgumentException("No active session"));
        session.setExitTime(LocalDateTime.now());
        long minutes = Duration.between(session.getEntryTime(), session.getExitTime()).toMinutes();
        session.setFee(pricingService.calculateTieredFee(Math.max(1, minutes)));
        session.setPaymentStatus("PENDING_GATEWAY");
        session.setActive(false);
        var spot = session.getSpot();
        spot.setOccupied(false);
        spotRepository.save(spot);
        sessionRepository.save(session);
        return new ExitResponse(session.getId(), session.getPlateNumber(), session.getFee(), session.getPaymentStatus());
    }

    public void scanOvertime() {
        sessionRepository.findByActiveTrueAndEntryTimeBefore(LocalDateTime.now().minusHours(24))
                .forEach(s -> createAlert("OVERTIME", s.getPlateNumber(), "Parking overtime > 24h"));
    }

    private void createAlert(String type, String plate, String message) {
        alertRepository.save(Alert.builder()
                .type(type)
                .plateNumber(plate)
                .message(message)
                .resolved(false)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
