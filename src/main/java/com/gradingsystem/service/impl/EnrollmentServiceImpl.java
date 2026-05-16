package com.gradingsystem.service.impl;

import com.gradingsystem.dto.enrollment.EnrollmentRequest;
import com.gradingsystem.dto.enrollment.EnrollmentResponse;
import com.gradingsystem.entity.Schedule;
import com.gradingsystem.entity.StudentSchedule;
import com.gradingsystem.entity.StudentSchedule.EnrollmentStatus;
import com.gradingsystem.entity.User;
import com.gradingsystem.exception.DuplicateResourceException;
import com.gradingsystem.exception.ResourceNotFoundException;
import com.gradingsystem.repository.ScheduleRepository;
import com.gradingsystem.repository.StudentScheduleRepository;
import com.gradingsystem.repository.UserRepository;
import com.gradingsystem.service.AuditLogService;
import com.gradingsystem.service.EnrollmentService;
import com.gradingsystem.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final StudentScheduleRepository enrollmentRepository;
    private final UserRepository            userRepository;
    private final ScheduleRepository        scheduleRepository;
    private final AuditLogService           auditLogService;

    @Override
    @Transactional
    public EnrollmentResponse enroll(EnrollmentRequest request) {
        User student = findStudentOrThrow(request.getStudentId());
        Schedule schedule = findScheduleOrThrow(request.getScheduleId());

        // Allow re-enroll only if previously DROPPED; reject any other active enrollment
        if (enrollmentRepository.existsByStudentIdAndScheduleIdAndStatusNot(
                student.getId(), schedule.getId(), EnrollmentStatus.DROPPED)) {
            throw new DuplicateResourceException("Student is already enrolled in this schedule");
        }

        // Reuse existing DROPPED record instead of creating a duplicate
        StudentSchedule enrollment = enrollmentRepository
            .findByStudentIdAndScheduleId(student.getId(), schedule.getId())
            .orElseGet(() -> StudentSchedule.builder()
                .student(student)
                .schedule(schedule)
                .build());

        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus(EnrollmentStatus.ENROLLED);

        StudentSchedule saved = enrollmentRepository.save(enrollment);
        auditLogService.log("ENROLL", "Enrollment", String.valueOf(saved.getId()));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public EnrollmentResponse dropEnrollment(Integer enrollmentId) {
        StudentSchedule enrollment = findOrThrow(enrollmentId);
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        StudentSchedule saved = enrollmentRepository.save(enrollment);
        auditLogService.log("DROP", "Enrollment", String.valueOf(enrollmentId));
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollmentById(Integer id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStudent(String studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", studentId);
        }
        return enrollmentRepository.findByStudentIdWithDetails(studentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsBySchedule(Integer scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new ResourceNotFoundException("Schedule", scheduleId);
        }
        return enrollmentRepository.findByScheduleIdWithDetails(scheduleId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMySubjects() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        return enrollmentRepository.findByStudentIdWithDetails(currentUserId)
            .stream()
            .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED)
            .map(this::toResponse)
            .toList();
    }

    // -----------------------------------------------------------------------

    private User findStudentOrThrow(String id) {
        User student = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        if (!"STUDENT".equals(student.getRole().getName())) {
            throw new IllegalArgumentException("User " + id + " does not have role STUDENT");
        }
        return student;
    }

    private Schedule findScheduleOrThrow(Integer id) {
        return scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule", id));
    }

    private StudentSchedule findOrThrow(Integer id) {
        return enrollmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));
    }

    private EnrollmentResponse toResponse(StudentSchedule e) {
        return EnrollmentResponse.builder()
            .id(e.getId())
            .studentId(e.getStudent().getId())
            .studentName(e.getStudent().getFullName())
            .scheduleId(e.getSchedule().getId())
            .subjectCode(e.getSchedule().getSubject().getCode())
            .subjectName(e.getSchedule().getSubject().getName())
            .schoolYear(e.getSchedule().getSchoolYear())
            .semester(e.getSchedule().getSemester())
            .enrollmentDate(e.getEnrollmentDate())
            .status(e.getStatus().name())
            .build();
    }
}
