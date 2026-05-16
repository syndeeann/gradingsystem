package com.gradingsystem.dto.student;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class StudentProfileResponse {

    private String id;
    private String username;
    private String email;
    private String fullName;
    private boolean active;
    private LocalDateTime createdAt;

    private BigDecimal gpa;
    private String honorLabel;

    private List<EnrollmentDetailDto> enrollments;
}
