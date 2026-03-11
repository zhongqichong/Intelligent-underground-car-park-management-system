package com.garage.service;

import com.garage.dto.AuthRequest;
import com.garage.dto.AuthResponse;
import com.garage.dto.RegisterRequest;
import com.garage.entity.Role;
import com.garage.entity.User;
import com.garage.repository.UserRepository;
import com.garage.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public void registerOwner(RegisterRequest request) {
        userRepository.findByUsername(request.username()).ifPresent(u -> {
            throw new IllegalArgumentException("Username exists");
        });
        userRepository.save(User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.OWNER)
                .build());
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        var user = userRepository.findByUsername(request.username()).orElseThrow();
        return new AuthResponse(jwtService.generateToken(user.getUsername(), user.getRole().name()));
    }
}
