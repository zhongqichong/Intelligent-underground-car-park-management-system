package com.garage.dto;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        String type,
        String message,
        String plateNumber,
        Boolean resolved,
        LocalDateTime createdAt
) {
}
