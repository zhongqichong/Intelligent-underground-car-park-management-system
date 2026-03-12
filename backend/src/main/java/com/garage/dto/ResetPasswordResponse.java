package com.garage.dto;

public record ResetPasswordResponse(
        Long userId,
        String message
) {
}
