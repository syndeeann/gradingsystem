package com.gradingsystem.dto.enrollment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnrollmentRequest {

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotNull(message = "Schedule ID is required")
    private Integer scheduleId;
}
