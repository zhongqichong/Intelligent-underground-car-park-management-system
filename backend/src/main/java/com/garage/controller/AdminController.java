package com.garage.controller;

import com.garage.dto.*;
import com.garage.entity.MapElement;
import com.garage.entity.OperationLog;
import com.garage.entity.ParkingSpot;
import com.garage.repository.OperationLogRepository;
import com.garage.repository.ParkingSpotRepository;
import com.garage.service.AdminService;
import com.garage.service.ReportService;
import com.garage.service.SimulationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    private final ParkingSpotRepository spotRepository;
    private final OperationLogRepository operationLogRepository;
    private final SimulationService simulationService;
    private final ReportService reportService;

    @PostMapping("/accounts/admin")
    public ApiResponse<AdminUserResponse> createAdmin(@Valid @RequestBody CreateAdminRequest body) {
        return ApiResponse.ok("Admin account created", adminService.createAdmin(body.username(), body.password()));
    }

    @PostMapping("/accounts/{userId}/password")
    public ApiResponse<ResetPasswordResponse> resetPassword(@PathVariable Long userId, @Valid @RequestBody ResetPasswordRequest body) {
        AdminUserResponse user = adminService.resetPassword(userId, body.password());
        return ApiResponse.ok(new ResetPasswordResponse(user.id(), "Password updated successfully"));
    }

    @GetMapping("/spots")
    public ApiResponse<List<ParkingSpot>> spots() {
        return ApiResponse.ok(spotRepository.findAll());
    }

    @PostMapping("/spots")
    public ApiResponse<ParkingSpot> upsertSpot(@RequestBody ParkingSpot spot) {
        return ApiResponse.ok("Spot saved", adminService.upsertSpot(spot));
    }

    @GetMapping("/map-elements")
    public ApiResponse<List<MapElement>> mapElements() {
        return ApiResponse.ok(adminService.listMapElements());
    }

    @PostMapping("/map-elements")
    public ApiResponse<MapElement> upsertMapElement(@RequestBody MapElement element) {
        return ApiResponse.ok("Map element saved", adminService.upsertMapElement(element));
    }

    @PostMapping("/simulate/tick")
    public ApiResponse<Map<String, Integer>> simulateTick() {
        return ApiResponse.ok(Map.of("updated", simulationService.simulateTrafficTick()));
    }

    @GetMapping("/reports/overview")
    public ApiResponse<DashboardOverviewResponse> reportOverview() {
        return ApiResponse.ok(reportService.overview());
    }

    @GetMapping("/alerts")
    public ApiResponse<List<AlertResponse>> alerts(@RequestParam(defaultValue = "false") boolean unresolvedOnly) {
        return ApiResponse.ok(adminService.listAlerts(unresolvedOnly));
    }

    @PostMapping("/alerts/{alertId}/resolve")
    public ApiResponse<AlertResponse> resolveAlert(@PathVariable Long alertId) {
        return ApiResponse.ok("Alert resolved", adminService.resolveAlert(alertId));
    }

    @GetMapping("/logs")
    public ApiResponse<List<OperationLog>> logs() {
        return ApiResponse.ok(operationLogRepository.findAll());
    }
}
