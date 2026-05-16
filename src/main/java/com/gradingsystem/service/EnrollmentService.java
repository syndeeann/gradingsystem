package com.gradingsystem.service;

import com.gradingsystem.dto.enrollment.EnrollmentRequest;
import com.gradingsystem.dto.enrollment.EnrollmentResponse;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponse enroll(EnrollmentRequest request);

    EnrollmentResponse dropEnrollment(Integer enrollmentId);

    EnrollmentResponse getEnrollmentById(Integer id);

    List<EnrollmentResponse> getEnrollmentsByStudent(String studentId);

    List<EnrollmentResponse> getEnrollmentsBySchedule(Integer scheduleId);

    List<EnrollmentResponse> getMySubjects();
}
