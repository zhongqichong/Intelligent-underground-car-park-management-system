package com.garage.repository;

import com.garage.entity.MapElement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MapElementRepository extends JpaRepository<MapElement, Long> {
}
