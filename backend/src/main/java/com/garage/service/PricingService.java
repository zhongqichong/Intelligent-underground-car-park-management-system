package com.garage.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {
    public BigDecimal calculateTieredFee(long totalMinutes) {
        long hours = (long) Math.ceil(totalMinutes / 60.0);
        if (hours <= 1) return BigDecimal.valueOf(5);
        if (hours <= 4) return BigDecimal.valueOf(5 + (hours - 1) * 4);
        return BigDecimal.valueOf(17 + (hours - 4) * 3);
    }
}
