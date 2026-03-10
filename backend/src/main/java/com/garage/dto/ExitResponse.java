package com.garage.dto;

import java.math.BigDecimal;

public record ExitResponse(Long sessionId, String plateNumber, BigDecimal fee, String paymentStatus) {}
