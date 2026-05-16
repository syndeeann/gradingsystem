package com.gradingsystem.dto.grade;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GradeRequest {

    @NotNull(message = "Enrollment ID is required")
    private Integer studentScheduleId;

    @NotNull(message = "Grade value is required")
    @DecimalMin(value = "0.00", message = "Grade must be at least 0")
    @DecimalMax(value = "100.00", message = "Grade must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Grade must have at most 2 decimal places")
    private BigDecimal gradeValue;

    @Size(max = 255)
    private String remarks;
}
