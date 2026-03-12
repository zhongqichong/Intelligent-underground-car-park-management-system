package com.garage.controller;

import com.garage.dto.*;
import com.garage.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register-owner")
    public ApiResponse<Void> registerOwner(@Valid @RequestBody RegisterRequest request) {
        authService.registerOwner(request);
        return ApiResponse.ok("Owner registered", null);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ApiResponse.ok("Login success", authService.login(request));
    }
}
