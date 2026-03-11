package com.garage.dto;

import jakarta.validation.constraints.NotBlank;

public record StripeWebhookRequest(
        @NotBlank String paymentIntentId,
        @NotBlank String status
) {
}
