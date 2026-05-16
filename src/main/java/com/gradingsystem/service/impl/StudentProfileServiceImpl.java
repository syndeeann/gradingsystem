package com.gradingsystem.service.impl;

import com.gradingsystem.dto.student.EnrollmentDetailDto;
import com.gradingsystem.dto.student.StudentProfileResponse;
import com.gradingsystem.entity.Grade;
import com.gradingsystem.entity.StudentSchedule;
import com.gradingsystem.entity.User;
import com.gradingsystem.exception.ResourceNotFoundException;
import com.gradingsystem.repository.GradeRepository;
import com.gradingsystem.repository.HonorThresholdRepository;
import com.gradingsystem.repository.StudentScheduleRepository;
import com.gradingsystem.repository.UserRepository;
import com.gradingsystem.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentProfileServiceImpl implements StudentProfileService {

    private final UserRepository            userRepository;
    private final StudentScheduleRepository enrollmentRepository;
    private final GradeRepository           gradeRepository;
    private final HonorThresholdRepository  honorThresholdRepository;

    @Override
    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(String studentId) {
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        // All enrollments for this student (eagerly fetched with schedule, subject, teacher)
        List<StudentSchedule> enrollments =
            enrollmentRepository.findByStudentIdWithDetails(studentId);

        // All grades for this student, keyed by enrollmentId for O(1) lookup
        Map<Integer, Grade> gradeByEnrollmentId =
            gradeRepository.findByStudentIdWithDetails(studentId)
                .stream()
                .collect(Collectors.toMap(
                    g -> g.getStudentSchedule().getId(),
                    Function.identity()
                ));

        List<EnrollmentDetailDto> enrollmentDtos = enrollments.stream()
            .map(ss -> buildEnrollmentDetail(ss, gradeByEnrollmentId.get(ss.getId())))
            .toList();

        BigDecimal gpa = gradeRepository.computeWeightedGpa(studentId)
            .map(g -> g.setScale(2, RoundingMode.HALF_UP))
            .orElse(BigDecimal.ZERO.setScale(2));

        String honorLabel = BigDecimal.ZERO.compareTo(gpa) == 0 ? null
            : honorThresholdRepository.findMatchingThreshold(gpa)
                .map(t -> t.getLabel())
                .orElse(null);

        return StudentProfileResponse.builder()
            .id(student.getId())
            .username(student.getUsername())
            .email(student.getEmail())
            .fullName(student.getFullName())
            .active(student.isActive())
            .createdAt(student.getCreatedAt())
            .gpa(gpa)
            .honorLabel(honorLabel)
            .enrollments(enrollmentDtos)
            .build();
    }

    private EnrollmentDetailDto buildEnrollmentDetail(StudentSchedule ss, Grade grade) {
        var schedule = ss.getSchedule();
        var subject  = schedule.getSubject();
        var teacher  = schedule.getTeacher();

        String honorLabel = null;
        if (grade != null) {
            honorLabel = honorThresholdRepository.findMatchingThreshold(grade.getGradeValue())
                .map(t -> t.getLabel())
                .orElse(null);
        }

        return EnrollmentDetailDto.builder()
            .enrollmentId(ss.getId())
            .enrollmentStatus(ss.getStatus().name())
            .enrollmentDate(ss.getEnrollmentDate())
            .subjectId(subject.getId())
            .subjectCode(subject.getCode())
            .subjectName(subject.getName())
            .units(subject.getUnits())
            .scheduleId(schedule.getId())
            .room(schedule.getRoom())
            .dayOfWeek(schedule.getDayOfWeek())
            .timeStart(schedule.getTimeStart())
            .timeEnd(schedule.getTimeEnd())
            .semester(schedule.getSemester())
            .schoolYear(schedule.getSchoolYear())
            .teacherId(teacher.getId())
            .teacherName(teacher.getFullName())
            .gradeValue(grade != null ? grade.getGradeValue() : null)
            .gradeRemarks(grade != null ? grade.getRemarks() : null)
            .honorLabel(honorLabel)
            .build();
    }
}
