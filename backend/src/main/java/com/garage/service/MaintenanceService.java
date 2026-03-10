package com.garage.service;

import com.garage.repository.ParkingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MaintenanceService {
    private final ParkingSessionRepository parkingSessionRepository;

    @Scheduled(cron = "${maintenance.cleanup.cron:0 0 3 * * *}")
    public void cleanupOldSessions() {
        parkingSessionRepository.deleteByExitTimeBefore(LocalDateTime.now().minusMonths(1));
    }
}
