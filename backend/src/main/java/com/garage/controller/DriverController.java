package com.garage.controller;

import com.garage.dto.*;
import com.garage.service.ParkingService;
import com.garage.service.PaymentService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {
    private final ParkingService parkingService;
    private final PaymentService paymentService;

    @GetMapping("/recommend")
    public ApiResponse<SpotRecommendationResponse> recommend() {
        return ApiResponse.ok(parkingService.recommend());
    }

    @GetMapping("/map-overview")
    public ApiResponse<MapOverviewResponse> mapOverview() {
        return ApiResponse.ok(parkingService.mapOverview());
    }

    @PostMapping("/entry")
    public ApiResponse<Object> entry(@Valid @RequestBody EntryRequest request) {
        return ApiResponse.ok("Entry success", parkingService.entry(request));
    }

    @PostMapping("/exit/{plateNumber}")
    public ApiResponse<ExitResponse> exit(@PathVariable String plateNumber) {
        return ApiResponse.ok("Exit success", parkingService.exit(plateNumber));
    }

    @PostMapping("/payment/{sessionId}")
    public ApiResponse<PaymentIntentResponse> pay(@PathVariable Long sessionId) throws StripeException {
        return ApiResponse.ok("Payment intent created", paymentService.createPaymentIntent(sessionId));
    }

    @PostMapping("/payment/webhook")
    public ApiResponse<Void> paymentWebhook(@Valid @RequestBody StripeWebhookRequest request) {
        paymentService.handleWebhook(request);
        return ApiResponse.ok("Webhook handled", null);
    }

    @PostMapping("/alerts/scan-overtime")
    public ApiResponse<Void> scanOvertime() {
        parkingService.scanOvertime();
        return ApiResponse.ok("Overtime scan done", null);
    }
}
