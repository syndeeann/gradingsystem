package com.gradingsystem.dto.enrollment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class EnrollmentResponse {

    private Integer id;
    private String studentId;
    private String studentName;
    private Integer scheduleId;
    private String subjectCode;
    private String subjectName;
    private String schoolYear;
    private Integer semester;
    private LocalDate enrollmentDate;
    private String status;
}
