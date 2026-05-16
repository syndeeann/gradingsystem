package com.gradingsystem.service.impl;

import com.gradingsystem.dto.grade.GradeRequest;
import com.gradingsystem.dto.grade.GradeResponse;
import com.gradingsystem.entity.Grade;
import com.gradingsystem.entity.StudentSchedule;
import com.gradingsystem.entity.User;
import com.gradingsystem.exception.DuplicateResourceException;
import com.gradingsystem.exception.ResourceNotFoundException;
import com.gradingsystem.exception.UnauthorizedException;
import com.gradingsystem.mapper.GradeMapper;
import com.gradingsystem.repository.GradeRepository;
import com.gradingsystem.repository.HonorThresholdRepository;
import com.gradingsystem.repository.StudentScheduleRepository;
import com.gradingsystem.repository.UserRepository;
import com.gradingsystem.security.UserPrincipal;
import com.gradingsystem.service.AuditLogService;
import com.gradingsystem.service.GradeService;
import com.gradingsystem.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository            gradeRepository;
    private final StudentScheduleRepository  enrollmentRepository;
    private final UserRepository             userRepository;
    private final HonorThresholdRepository   honorThresholdRepository;
    private final GradeMapper                gradeMapper;
    private final AuditLogService            auditLogService;

    @Override
    @Transactional
    public GradeResponse recordGrade(GradeRequest request) {
        StudentSchedule enrollment = enrollmentRepository.findByIdWithDetails(request.getStudentScheduleId())
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment", request.getStudentScheduleId()));

        if (gradeRepository.findByStudentScheduleId(request.getStudentScheduleId()).isPresent()) {
            throw new DuplicateResourceException("Grade already recorded for this enrollment");
        }

        authorizeGradeWrite(enrollment);

        String recorderId = SecurityUtils.getCurrentUserId();
        User recorder = userRepository.findById(recorderId)
            .orElseThrow(() -> new ResourceNotFoundException("User", recorderId));

        Grade grade = Grade.builder()
            .studentSchedule(enrollment)
            .gradeValue(request.getGradeValue())
            .remarks(request.getRemarks())
            .recordedBy(recorder)
            .build();

        Grade saved = gradeRepository.save(grade);
        auditLogService.log("CREATE", "Grade", String.valueOf(saved.getId()));
        return withHonorLabel(gradeMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public GradeResponse updateGrade(Integer id, GradeRequest request) {
        Grade grade = findOrThrow(id);

        String recorderId = SecurityUtils.getCurrentUserId();
        User recorder = userRepository.findById(recorderId)
            .orElseThrow(() -> new ResourceNotFoundException("User", recorderId));

        grade.setGradeValue(request.getGradeValue());
        grade.setRemarks(request.getRemarks());
        grade.setRecordedBy(recorder);

        Grade saved = gradeRepository.save(grade);
        auditLogService.log("UPDATE", "Grade", String.valueOf(id));
        return withHonorLabel(gradeMapper.toResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> getGradesByStudent(String studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", studentId);
        }
        return gradeRepository.findByStudentIdWithDetails(studentId)
            .stream()
            .map(gradeMapper::toResponse)
            .map(this::withHonorLabel)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> getGradesBySchedule(Integer scheduleId) {
        return gradeRepository.findByScheduleIdWithDetails(scheduleId)
            .stream()
            .map(gradeMapper::toResponse)
            .map(this::withHonorLabel)
            .toList();
    }

    // -----------------------------------------------------------------------

    private void authorizeGradeWrite(StudentSchedule enrollment) {
        UserPrincipal principal = SecurityUtils.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("Not authenticated"));

        boolean isPrivileged = principal.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                       || a.getAuthority().equals("ROLE_REGISTRAR"));
        if (isPrivileged) return;

        String assignedTeacherId = enrollment.getSchedule().getTeacher().getId();
        if (!principal.getId().equals(assignedTeacherId)) {
            throw new UnauthorizedException(
                "Only the assigned teacher, an Admin, or Registrar may record grades for this schedule");
        }
    }

    private GradeResponse withHonorLabel(GradeResponse response) {
        if (response.getGradeValue() == null) return response;
        return honorThresholdRepository.findMatchingThreshold(response.getGradeValue())
            .map(t -> response.toBuilder().honorLabel(t.getLabel()).build())
            .orElse(response);
    }

    private Grade findOrThrow(Integer id) {
        return gradeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Grade", id));
    }
}
