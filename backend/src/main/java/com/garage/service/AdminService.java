package com.garage.service;

import com.garage.dto.AdminUserResponse;
import com.garage.dto.AlertResponse;
import com.garage.entity.MapElement;
import com.garage.entity.MapElementType;
import com.garage.entity.ParkingSpot;
import com.garage.entity.Role;
import com.garage.entity.User;
import com.garage.repository.AlertRepository;
import com.garage.repository.MapElementRepository;
import com.garage.repository.ParkingSpotRepository;
import com.garage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ParkingSpotRepository spotRepository;
    private final MapElementRepository mapElementRepository;
    private final AlertRepository alertRepository;

    public AdminUserResponse createAdmin(String username, String password) {
        userRepository.findByUsername(username).ifPresent(u -> {
            throw new IllegalArgumentException("Username already exists");
        });
        User saved = userRepository.save(User.builder().username(username)
                .password(passwordEncoder.encode(password)).role(Role.ADMIN).build());
        return toAdminUserResponse(saved);
    }

    public AdminUserResponse resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        User saved = userRepository.save(user);
        return toAdminUserResponse(saved);
    }

    public List<ParkingSpot> listSpots() {
        return spotRepository.findAll();
    }

    public ParkingSpot upsertSpot(ParkingSpot spot) {
        return spotRepository.save(spot);
    }

    public ParkingSpot updateSpotStatus(Long spotId, boolean occupied) {
        ParkingSpot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new IllegalArgumentException("Spot not found"));
        spot.setOccupied(occupied);
        return spotRepository.save(spot);
    }

    public void deleteSpot(Long spotId) {
        if (!spotRepository.existsById(spotId)) {
            throw new IllegalArgumentException("Spot not found");
        }
        spotRepository.deleteById(spotId);
    }

    public MapElement upsertMapElement(MapElement element) {
        if (element.getType() == MapElementType.ENTRANCE) {
            mapElementRepository.findAll().stream()
                    .filter(e -> e.getType() == MapElementType.ENTRANCE)
                    .filter(e -> element.getId() == null || !e.getId().equals(element.getId()))
                    .forEach(mapElementRepository::delete);
        }
        return mapElementRepository.save(element);
    }

    public List<MapElement> listMapElements() {
        return mapElementRepository.findAll();
    }

    public void deleteMapElement(Long elementId) {
        if (!mapElementRepository.existsById(elementId)) {
            throw new IllegalArgumentException("Map element not found");
        }
        mapElementRepository.deleteById(elementId);
    }

    public List<AlertResponse> listAlerts(boolean unresolvedOnly) {
        List<com.garage.entity.Alert> alerts = unresolvedOnly
                ? alertRepository.findByResolvedFalseOrderByCreatedAtDesc()
                : alertRepository.findAllByOrderByCreatedAtDesc();
        return alerts.stream().map(this::toAlertResponse).toList();
    }

    public AlertResponse resolveAlert(Long alertId) {
        var alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        alert.setResolved(true);
        return toAlertResponse(alertRepository.save(alert));
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        return new AdminUserResponse(user.getId(), user.getUsername(), user.getRole());
    }

    private AlertResponse toAlertResponse(com.garage.entity.Alert a) {
        return new AlertResponse(a.getId(), a.getType(), a.getMessage(), a.getPlateNumber(), a.getResolved(), a.getCreatedAt());
    }
}
