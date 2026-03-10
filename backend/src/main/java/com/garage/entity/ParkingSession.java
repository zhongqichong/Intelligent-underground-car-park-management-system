package com.garage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParkingSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String plateNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    private ParkingSpot spot;

    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private BigDecimal fee;
    private String paymentStatus;
    private String paymentIntentId;
    private Boolean active;
}
