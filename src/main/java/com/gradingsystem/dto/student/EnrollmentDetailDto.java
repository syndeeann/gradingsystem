package com.gradingsystem.dto.student;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class EnrollmentDetailDto {

    private Integer enrollmentId;
    private String enrollmentStatus;
    private LocalDate enrollmentDate;

    private Integer subjectId;
    private String subjectCode;
    private String subjectName;
    private Integer units;

    private Integer scheduleId;
    private String room;
    private String dayOfWeek;
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private Integer semester;
    private String schoolYear;

    private String teacherId;
    private String teacherName;

    private BigDecimal gradeValue;
    private String gradeRemarks;
    private String honorLabel;
}
