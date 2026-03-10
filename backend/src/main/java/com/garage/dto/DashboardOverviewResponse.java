package com.garage.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardOverviewResponse(
        long totalSpots,
        long occupiedSpots,
        long freeSpots,
        long activeSessions,
        BigDecimal todayRevenue,
        long todayEntries,
        long unresolvedAlerts,
        List<DailyRevenuePoint> recentDailyRevenue
) {
    public record DailyRevenuePoint(String day, BigDecimal revenue) {}
}
