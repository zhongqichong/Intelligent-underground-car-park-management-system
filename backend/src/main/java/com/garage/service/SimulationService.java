package com.garage.service;

import com.garage.repository.ParkingSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class SimulationService {
    private final ParkingSpotRepository parkingSpotRepository;
    private final Random random = new Random();

    public int simulateTrafficTick() {
        var spots = parkingSpotRepository.findAll();
        int updates = 0;
        for (var spot : spots) {
            if (random.nextDouble() < 0.12) {
                spot.setZoneLoad(Math.max(1, Math.min(10, spot.getZoneLoad() + random.nextInt(3) - 1)));
                updates++;
            }
            if (random.nextDouble() < 0.05) {
                spot.setOccupied(!Boolean.TRUE.equals(spot.getOccupied()));
                updates++;
            }
        }
        parkingSpotRepository.saveAll(spots);
        return updates;
    }
}
