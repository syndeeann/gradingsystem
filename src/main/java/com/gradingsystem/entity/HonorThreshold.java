package com.gradingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "honor_thresholds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HonorThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String label;

    @Column(name = "min_grade", nullable = false, precision = 5, scale = 2)
    private BigDecimal minGrade;

    @Column(name = "max_grade", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxGrade;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
