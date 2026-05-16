package com.gradingsystem.dto.honor;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class HonorThresholdDto {

    private Integer id;
    private String label;
    private BigDecimal minGrade;
    private BigDecimal maxGrade;
    private boolean active;
    private LocalDateTime createdAt;
}
