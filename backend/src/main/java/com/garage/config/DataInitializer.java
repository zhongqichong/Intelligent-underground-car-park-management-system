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

import java.util.ArrayList;
import java.util.List;

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
            List<ParkingSpot> spots = new ArrayList<>();
            int index = 1;

            int[] rowGroupStarts = new int[]{3, 11, 19};
            int[] colGroupStarts = new int[]{4, 16, 28, 40};

            for (int rowGroupStart : rowGroupStarts) {
                for (int rowOffset : new int[]{0, 2}) {
                    int y = rowGroupStart + rowOffset;
                    for (int colGroupStart : colGroupStarts) {
                        for (int i = 0; i < 5; i++) {
                            int x = colGroupStart + i * 2;
                            spots.add(ParkingSpot.builder()
                                    .code("A" + index)
                                    .x(x)
                                    .y(y)
                                    .zoneLoad((index % 5) + 1)
                                    .occupied(false)
                                    .nearPillar(index % 8 == 0)
                                    .nearElevator(y <= 7)
                                    .build());
                            index++;
                        }
                    }
                }
            }
            parkingSpotRepository.saveAll(spots);
        }

        if (mapElementRepository.count() == 0) {
            List<MapElement> elements = new ArrayList<>();
            elements.add(MapElement.builder().type(MapElementType.ELEVATOR).x(30).y(2).width(2).height(2).label("Main-Elevator").build());
            elements.add(MapElement.builder().type(MapElementType.ENTRANCE).x(1).y(17).width(2).height(2).label("Gate-1").build());

            elements.add(MapElement.builder().type(MapElementType.BOUNDARY).x(0).y(0).width(60).height(1).label("North").build());
            elements.add(MapElement.builder().type(MapElementType.BOUNDARY).x(0).y(34).width(60).height(1).label("South").build());
            elements.add(MapElement.builder().type(MapElementType.BOUNDARY).x(0).y(0).width(1).height(35).label("West").build());
            elements.add(MapElement.builder().type(MapElementType.BOUNDARY).x(59).y(0).width(1).height(35).label("East").build());

            elements.add(MapElement.builder().type(MapElementType.PILLAR).x(14).y(8).width(2).height(2).label("P1").build());
            elements.add(MapElement.builder().type(MapElementType.PILLAR).x(24).y(12).width(2).height(2).label("P2").build());
            elements.add(MapElement.builder().type(MapElementType.PILLAR).x(35).y(18).width(2).height(2).label("P3").build());
            elements.add(MapElement.builder().type(MapElementType.PILLAR).x(45).y(24).width(2).height(2).label("P4").build());

            elements.add(MapElement.builder().type(MapElementType.NO_PARKING).x(20).y(5).width(6).height(2).label("NoPark-1").build());
            elements.add(MapElement.builder().type(MapElementType.NO_PARKING).x(38).y(27).width(7).height(2).label("NoPark-2").build());

            mapElementRepository.saveAll(elements);
        }

        if (blacklistVehicleRepository.count() == 0) {
            blacklistVehicleRepository.save(BlacklistVehicle.builder().plateNumber("TEST-BLACK-001").reason("Outstanding debt").build());
        }
    }
}
