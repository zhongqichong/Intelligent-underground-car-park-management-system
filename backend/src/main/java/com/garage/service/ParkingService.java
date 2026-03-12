package com.garage.service;

import com.garage.dto.EntryRequest;
import com.garage.dto.ExitResponse;
import com.garage.dto.MapOverviewResponse;
import com.garage.dto.SpotRecommendationResponse;
import com.garage.entity.Alert;
import com.garage.entity.MapElement;
import com.garage.entity.MapElementType;
import com.garage.entity.ParkingSession;
import com.garage.entity.ParkingSpot;
import com.garage.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ParkingService {
    public static final int MAP_WIDTH = 60;
    public static final int MAP_HEIGHT = 35;

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
        var route = generateRoute(elements, spots, recommendedSpot);
        return new MapOverviewResponse(MAP_WIDTH, MAP_HEIGHT, spots, elements, recommendation, route);
    }

    List<MapOverviewResponse.RoutePoint> generateRoute(List<MapElement> elements, List<ParkingSpot> spots, ParkingSpot targetSpot) {
        var entrance = elements.stream()
                .filter(e -> e.getType() == MapElementType.ENTRANCE)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No entrance configured"));

        int startX = entrance.getX();
        int startY = entrance.getY();
        int targetX = targetSpot.getX();
        int targetY = targetSpot.getY();

        boolean[][] blocked = new boolean[MAP_WIDTH][MAP_HEIGHT];

        for (MapElement element : elements) {
            if (element.getType() == MapElementType.ENTRANCE || element.getType() == MapElementType.ELEVATOR) {
                continue;
            }
            if (element.getType() == MapElementType.BOUNDARY || element.getType() == MapElementType.PILLAR || element.getType() == MapElementType.NO_PARKING) {
                int width = Optional.ofNullable(element.getWidth()).orElse(1);
                int height = Optional.ofNullable(element.getHeight()).orElse(1);
                for (int x = element.getX(); x < element.getX() + width; x++) {
                    for (int y = element.getY(); y < element.getY() + height; y++) {
                        if (inMap(x, y)) blocked[x][y] = true;
                    }
                }
            }
        }

        for (ParkingSpot spot : spots) {
            if (Boolean.TRUE.equals(spot.getOccupied()) && !Objects.equals(spot.getId(), targetSpot.getId()) && inMap(spot.getX(), spot.getY())) {
                blocked[spot.getX()][spot.getY()] = true;
            }
        }

        blocked[startX][startY] = false;
        blocked[targetX][targetY] = false;

        return aStarPath(startX, startY, targetX, targetY, blocked);
    }

    private List<MapOverviewResponse.RoutePoint> aStarPath(int sx, int sy, int tx, int ty, boolean[][] blocked) {
        record Node(int x, int y, int g, int f) {}

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(Node::f));
        Map<String, Integer> gScore = new HashMap<>();
        Map<String, String> cameFrom = new HashMap<>();

        String startKey = key(sx, sy);
        String targetKey = key(tx, ty);
        gScore.put(startKey, 0);
        open.add(new Node(sx, sy, 0, heuristic(sx, sy, tx, ty)));

        int[][] dirs = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!open.isEmpty()) {
            Node current = open.poll();
            String currentKey = key(current.x(), current.y());
            if (currentKey.equals(targetKey)) {
                return reconstructPath(cameFrom, currentKey);
            }

            for (int[] d : dirs) {
                int nx = current.x() + d[0];
                int ny = current.y() + d[1];
                if (!inMap(nx, ny) || blocked[nx][ny]) {
                    continue;
                }
                String nextKey = key(nx, ny);
                int tentativeG = current.g() + 1;
                if (tentativeG < gScore.getOrDefault(nextKey, Integer.MAX_VALUE)) {
                    cameFrom.put(nextKey, currentKey);
                    gScore.put(nextKey, tentativeG);
                    int f = tentativeG + heuristic(nx, ny, tx, ty);
                    open.add(new Node(nx, ny, tentativeG, f));
                }
            }
        }

        return List.of(new MapOverviewResponse.RoutePoint(sx, sy), new MapOverviewResponse.RoutePoint(tx, ty));
    }

    private List<MapOverviewResponse.RoutePoint> reconstructPath(Map<String, String> cameFrom, String currentKey) {
        List<MapOverviewResponse.RoutePoint> route = new ArrayList<>();
        String cursor = currentKey;
        while (cursor != null) {
            int[] xy = parseKey(cursor);
            route.add(new MapOverviewResponse.RoutePoint(xy[0], xy[1]));
            cursor = cameFrom.get(cursor);
        }
        Collections.reverse(route);
        return route;
    }

    private int heuristic(int x, int y, int tx, int ty) {
        return Math.abs(x - tx) + Math.abs(y - ty);
    }

    private String key(int x, int y) {
        return x + ":" + y;
    }

    private int[] parseKey(String key) {
        String[] parts = key.split(":");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    private boolean inMap(int x, int y) {
        return x >= 0 && y >= 0 && x < MAP_WIDTH && y < MAP_HEIGHT;
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
