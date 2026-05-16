package com.gradingsystem.mapper;

import com.gradingsystem.dto.schedule.ScheduleResponse;
import com.gradingsystem.entity.Schedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    @Mapping(target = "subjectId",   source = "subject.id")
    @Mapping(target = "subjectCode", source = "subject.code")
    @Mapping(target = "subjectName", source = "subject.name")
    @Mapping(target = "teacherId",   source = "teacher.id")
    @Mapping(target = "teacherName", source = "teacher.fullName")
    ScheduleResponse toResponse(Schedule schedule);
}
