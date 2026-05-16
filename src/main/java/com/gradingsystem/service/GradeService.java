package com.gradingsystem.service;

import com.gradingsystem.dto.grade.GradeRequest;
import com.gradingsystem.dto.grade.GradeResponse;

import java.util.List;

public interface GradeService {

    GradeResponse recordGrade(GradeRequest request);

    GradeResponse updateGrade(Integer id, GradeRequest request);

    List<GradeResponse> getGradesByStudent(String studentId);

    List<GradeResponse> getGradesBySchedule(Integer scheduleId);
}
