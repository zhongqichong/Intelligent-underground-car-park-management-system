package com.garage.repository;

import com.garage.entity.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    List<ParkingSpot> findByOccupiedFalse();
    ParkingSpot findByCode(String code);
}
