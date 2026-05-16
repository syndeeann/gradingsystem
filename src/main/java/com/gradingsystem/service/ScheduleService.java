package com.gradingsystem.service;

import com.gradingsystem.dto.common.PageResponse;
import com.gradingsystem.dto.schedule.ScheduleRequest;
import com.gradingsystem.dto.schedule.ScheduleResponse;

import java.util.List;

public interface ScheduleService {

    ScheduleResponse createSchedule(ScheduleRequest request);

    ScheduleResponse getScheduleById(Integer id);

    PageResponse<ScheduleResponse> listSchedules(
        String teacherId, Integer subjectId, Integer semester, String schoolYear,
        int page, int size
    );

    List<ScheduleResponse> getMySchedule();

    ScheduleResponse updateSchedule(Integer id, ScheduleRequest request);

    ScheduleResponse deactivateSchedule(Integer id);
}
