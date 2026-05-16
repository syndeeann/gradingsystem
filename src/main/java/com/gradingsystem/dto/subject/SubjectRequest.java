package com.gradingsystem.dto.subject;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectRequest {

    @NotBlank(message = "Subject code is required")
    @Size(max = 20)
    private String code;

    @NotBlank(message = "Subject name is required")
    @Size(max = 150)
    private String name;

    private String description;

    @NotNull(message = "Units are required")
    @Min(value = 1, message = "Units must be at least 1")
    @Max(value = 9, message = "Units must not exceed 9")
    private Integer units;
}
