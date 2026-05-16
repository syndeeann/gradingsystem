package com.gradingsystem.service.impl;

import com.gradingsystem.dto.common.PageResponse;
import com.gradingsystem.dto.schedule.ScheduleRequest;
import com.gradingsystem.dto.schedule.ScheduleResponse;
import com.gradingsystem.entity.Schedule;
import com.gradingsystem.entity.Subject;
import com.gradingsystem.entity.User;
import com.gradingsystem.exception.ResourceNotFoundException;
import com.gradingsystem.mapper.ScheduleMapper;
import com.gradingsystem.repository.ScheduleRepository;
import com.gradingsystem.repository.SubjectRepository;
import com.gradingsystem.repository.UserRepository;
import com.gradingsystem.service.AuditLogService;
import com.gradingsystem.service.ScheduleService;
import com.gradingsystem.util.PageUtils;
import com.gradingsystem.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final SubjectRepository  subjectRepository;
    private final UserRepository     userRepository;
    private final ScheduleMapper     scheduleMapper;
    private final AuditLogService    auditLogService;

    @Override
    @Transactional
    public ScheduleResponse createSchedule(ScheduleRequest request) {
        Subject subject = findSubjectOrThrow(request.getSubjectId());
        User teacher = findTeacherOrThrow(request.getTeacherId());

        detectConflict(teacher.getId(), request.getSemester(), request.getSchoolYear(),
            request.getDayOfWeek(), request.getTimeStart(), request.getTimeEnd(), -1);

        Schedule schedule = Schedule.builder()
            .subject(subject)
            .teacher(teacher)
            .room(request.getRoom())
            .dayOfWeek(request.getDayOfWeek())
            .timeStart(request.getTimeStart())
            .timeEnd(request.getTimeEnd())
            .semester(request.getSemester())
            .schoolYear(request.getSchoolYear())
            .build();

        Schedule saved = scheduleRepository.save(schedule);
        auditLogService.log("CREATE", "Schedule", String.valueOf(saved.getId()));
        return scheduleMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleById(Integer id) {
        return scheduleMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ScheduleResponse> listSchedules(
            String teacherId, Integer subjectId, Integer semester, String schoolYear,
            int page, int size) {
        Pageable pageable = PageUtils.toPageable(page, size, "id", "asc");
        return PageUtils.toPageResponse(
            scheduleRepository.findWithFilters(
                blankToNull(teacherId), subjectId, semester, blankToNull(schoolYear), pageable),
            scheduleMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getMySchedule() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        return scheduleRepository.findByTeacherIdAndActiveTrue(currentUserId)
            .stream()
            .map(scheduleMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public ScheduleResponse updateSchedule(Integer id, ScheduleRequest request) {
        Schedule schedule = findOrThrow(id);
        Subject subject = findSubjectOrThrow(request.getSubjectId());
        User teacher = findTeacherOrThrow(request.getTeacherId());

        detectConflict(teacher.getId(), request.getSemester(), request.getSchoolYear(),
            request.getDayOfWeek(), request.getTimeStart(), request.getTimeEnd(), id);

        schedule.setSubject(subject);
        schedule.setTeacher(teacher);
        schedule.setRoom(request.getRoom());
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setTimeStart(request.getTimeStart());
        schedule.setTimeEnd(request.getTimeEnd());
        schedule.setSemester(request.getSemester());
        schedule.setSchoolYear(request.getSchoolYear());

        Schedule saved = scheduleRepository.save(schedule);
        auditLogService.log("UPDATE", "Schedule", String.valueOf(id));
        return scheduleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ScheduleResponse deactivateSchedule(Integer id) {
        Schedule schedule = findOrThrow(id);
        schedule.setActive(false);
        Schedule saved = scheduleRepository.save(schedule);
        auditLogService.log("DEACTIVATE", "Schedule", String.valueOf(id));
        return scheduleMapper.toResponse(saved);
    }

    // -----------------------------------------------------------------------

    private void detectConflict(String teacherId, Integer semester, String schoolYear,
                                String dayOfWeek, LocalTime start, LocalTime end,
                                Integer excludeId) {
        List<Schedule> existing = scheduleRepository.findTeacherSchedulesForConflictCheck(
            teacherId, semester, schoolYear, excludeId);

        Set<String> newDays = splitDays(dayOfWeek);
        for (Schedule s : existing) {
            Set<String> existingDays = splitDays(s.getDayOfWeek());
            existingDays.retainAll(newDays);
            if (existingDays.isEmpty()) continue;
            if (start.isBefore(s.getTimeEnd()) && end.isAfter(s.getTimeStart())) {
                throw new IllegalStateException(
                    "Teacher has a conflicting schedule on " + String.join("/", existingDays)
                    + " from " + s.getTimeStart() + " to " + s.getTimeEnd());
            }
        }
    }

    private static Set<String> splitDays(String dayOfWeek) {
        return Arrays.stream(dayOfWeek.split("[/,;\\s]+"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }

    private Schedule findOrThrow(Integer id) {
        return scheduleRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule", id));
    }

    private Subject findSubjectOrThrow(Integer id) {
        return subjectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subject", id));
    }

    private User findTeacherOrThrow(String id) {
        User teacher = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher", id));
        if (!"TEACHER".equals(teacher.getRole().getName())) {
            throw new IllegalArgumentException("User " + id + " does not have role TEACHER");
        }
        return teacher;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
