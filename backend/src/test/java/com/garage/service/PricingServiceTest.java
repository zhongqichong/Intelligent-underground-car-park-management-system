package com.garage.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {
    private final PricingService pricingService = new PricingService();

    @Test
    void shouldCalculateTieredPrice() {
        assertEquals(BigDecimal.valueOf(5), pricingService.calculateTieredFee(30));
        assertEquals(BigDecimal.valueOf(9), pricingService.calculateTieredFee(90));
        assertEquals(BigDecimal.valueOf(20), pricingService.calculateTieredFee(330));
    }
}
