package com.gradingsystem.dto.schedule;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class ScheduleRequest {

    @NotNull(message = "Subject ID is required")
    private Integer subjectId;

    @NotBlank(message = "Teacher ID is required")
    private String teacherId;

    @Size(max = 50)
    private String room;

    @NotBlank(message = "Day of week is required")
    @Size(max = 30)
    private String dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime timeStart;

    @NotNull(message = "End time is required")
    private LocalTime timeEnd;

    @NotNull(message = "Semester is required")
    @Min(1) @Max(3)
    private Integer semester;

    @NotBlank(message = "School year is required")
    @Pattern(regexp = "\\d{4}-\\d{4}", message = "School year must be in format YYYY-YYYY")
    private String schoolYear;

    @AssertTrue(message = "End time must be after start time")
    public boolean isTimeRangeValid() {
        if (timeStart == null || timeEnd == null) return true;
        return timeEnd.isAfter(timeStart);
    }
}
