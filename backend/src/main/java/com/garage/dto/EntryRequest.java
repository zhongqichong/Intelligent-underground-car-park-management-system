package com.garage.dto;

import jakarta.validation.constraints.NotBlank;

public record EntryRequest(@NotBlank String plateNumber) {}
