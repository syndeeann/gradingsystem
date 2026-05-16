package com.gradingsystem.mapper;

import com.gradingsystem.dto.grade.GradeResponse;
import com.gradingsystem.entity.Grade;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GradeMapper {

    @Mapping(target = "studentScheduleId", source = "studentSchedule.id")
    @Mapping(target = "studentId",         source = "studentSchedule.student.id")
    @Mapping(target = "studentName",       source = "studentSchedule.student.fullName")
    @Mapping(target = "subjectCode",       source = "studentSchedule.schedule.subject.code")
    @Mapping(target = "subjectName",       source = "studentSchedule.schedule.subject.name")
    @Mapping(target = "units",             source = "studentSchedule.schedule.subject.units")
    @Mapping(target = "recordedById",      source = "recordedBy.id")
    @Mapping(target = "recordedByName",    source = "recordedBy.fullName")
    @Mapping(target = "honorLabel",        ignore = true)
    GradeResponse toResponse(Grade grade);
}
