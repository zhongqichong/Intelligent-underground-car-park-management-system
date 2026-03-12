package com.garage.dto;

import com.garage.entity.MapElement;
import com.garage.entity.ParkingSpot;

import java.util.List;

public record MapOverviewResponse(
        int width,
        int height,
        List<ParkingSpot> spots,
        List<MapElement> elements,
        SpotRecommendationResponse recommendation,
        List<RoutePoint> routePath
) {
    public record RoutePoint(int x, int y) {}
}
