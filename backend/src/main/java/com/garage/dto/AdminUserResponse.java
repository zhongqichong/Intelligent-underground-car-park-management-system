package com.garage.dto;

import com.garage.entity.Role;

public record AdminUserResponse(
        Long id,
        String username,
        Role role
) {
}
