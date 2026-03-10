package com.garage.config;

import com.garage.entity.*;
import com.garage.repository.BlacklistVehicleRepository;
import com.garage.repository.MapElementRepository;
import com.garage.repository.ParkingSpotRepository;
import com.garage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ParkingSpotRepository parkingSpotRepository;
    private final BlacklistVehicleRepository blacklistVehicleRepository;
    private final MapElementRepository mapElementRepository;

    @Override
    public void run(String... args) {
        userRepository.findByUsername("admin").orElseGet(() -> userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .build()));

        if (parkingSpotRepository.count() == 0) {
            IntStream.rangeClosed(1, 60).forEach(i -> parkingSpotRepository.save(ParkingSpot.builder()
                    .code("A" + i)
                    .x((i - 1) % 10 + 2)
                    .y((i - 1) / 10 + 2)
                    .zoneLoad((i % 5) + 1)
                    .occupied(false)
                    .nearPillar(i % 9 == 0)
                    .nearElevator(i <= 10)
                    .build()));
        }

        if (mapElementRepository.count() == 0) {
            mapElementRepository.saveAll(List.of(
                    MapElement.builder().type(MapElementType.ELEVATOR).x(1).y(1).width(1).height(1).label("Elevator").build(),
                    MapElement.builder().type(MapElementType.ENTRANCE).x(0).y(6).width(1).height(1).label("Gate-1").build(),
                    MapElement.builder().type(MapElementType.PILLAR).x(6).y(4).width(1).height(1).label("P1").build(),
                    MapElement.builder().type(MapElementType.PILLAR).x(11).y(5).width(1).height(1).label("P2").build(),
                    MapElement.builder().type(MapElementType.NO_PARKING).x(15).y(2).width(2).height(1).label("NoPark").build(),
                    MapElement.builder().type(MapElementType.BOUNDARY).x(0).y(0).width(40).height(1).label("North").build()
            ));
        }

        if (blacklistVehicleRepository.count() == 0) {
            blacklistVehicleRepository.save(BlacklistVehicle.builder().plateNumber("TEST-BLACK-001").reason("Outstanding debt").build());
        }
    }
}
