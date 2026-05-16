package com.gradingsystem.dto.honor;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder(toBuilder = true)
public class HonorStudentDto {

    private int rank;
    private String studentId;
    private String studentName;
    private String username;
    private BigDecimal gpa;
    private String honorLabel;
}
