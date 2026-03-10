package com.garage.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parking_spots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParkingSpot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String code;
    private Integer x;
    private Integer y;
    private Integer zoneLoad;
    private Boolean occupied;
    private Boolean nearPillar;
    private Boolean nearElevator;
}
