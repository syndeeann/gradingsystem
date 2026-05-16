package com.gradingsystem.dto.honor;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class HonorThresholdRequest {

    @NotBlank(message = "Label is required")
    @Size(max = 50)
    private String label;

    @NotNull(message = "Minimum grade is required")
    @DecimalMin(value = "0.00") @DecimalMax(value = "100.00")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal minGrade;

    @NotNull(message = "Maximum grade is required")
    @DecimalMin(value = "0.00") @DecimalMax(value = "100.00")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal maxGrade;

    @AssertTrue(message = "maxGrade must be greater than minGrade")
    public boolean isRangeValid() {
        if (minGrade == null || maxGrade == null) return true;
        return maxGrade.compareTo(minGrade) > 0;
    }
}
