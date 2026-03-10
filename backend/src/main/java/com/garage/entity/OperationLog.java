package com.garage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "operation_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OperationLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String action;
    private String method;
    private LocalDateTime createdAt;
}
