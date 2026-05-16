package com.gradingsystem.dto.grade;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class GradeResponse {

    private Integer id;
    private Integer studentScheduleId;
    private String studentId;
    private String studentName;
    private String subjectCode;
    private String subjectName;
    private Integer units;
    private BigDecimal gradeValue;
    private String remarks;
    private String honorLabel;
    private String recordedById;
    private String recordedByName;
    private LocalDateTime recordedAt;
    private LocalDateTime updatedAt;
}
