package com.garage.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "map_elements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MapElement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MapElementType type;

    @Column(nullable = false)
    private Integer x;

    @Column(nullable = false)
    private Integer y;

    private Integer width;
    private Integer height;
    private String label;
}
