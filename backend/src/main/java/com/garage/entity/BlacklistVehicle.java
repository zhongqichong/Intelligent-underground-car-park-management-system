package com.garage.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blacklist_vehicles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BlacklistVehicle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String plateNumber;
    private String reason;
}
