package com.garage.repository;

import com.garage.entity.BlacklistVehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistVehicleRepository extends JpaRepository<BlacklistVehicle, Long> {
    boolean existsByPlateNumber(String plateNumber);
}
