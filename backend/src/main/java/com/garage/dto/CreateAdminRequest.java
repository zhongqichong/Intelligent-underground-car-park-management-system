package com.garage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAdminRequest(
        @NotBlank @Size(min = 4, max = 64) String username,
        @NotBlank @Size(min = 8, max = 64) String password
) {
}
