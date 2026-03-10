package com.garage.repository;

import com.garage.entity.ParkingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {
    Optional<ParkingSession> findByPlateNumberAndActiveTrue(String plateNumber);
    List<ParkingSession> findByActiveTrueAndEntryTimeBefore(LocalDateTime threshold);
    List<ParkingSession> findBySpotIdAndActiveTrue(Long spotId);
    Optional<ParkingSession> findByPaymentIntentId(String paymentIntentId);
    long deleteByExitTimeBefore(LocalDateTime threshold);
}
