package com.gradingsystem.service;

import com.gradingsystem.dto.honor.HonorStudentDto;
import com.gradingsystem.dto.honor.HonorThresholdDto;
import com.gradingsystem.dto.honor.HonorThresholdRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface HonorService {

    List<HonorThresholdDto> getThresholds();

    HonorThresholdDto createThreshold(HonorThresholdRequest request);

    HonorThresholdDto updateThreshold(Integer id, HonorThresholdRequest request);

    HonorThresholdDto deactivateThreshold(Integer id);

    BigDecimal computeGpa(String studentId, Integer semester, String schoolYear);

    Optional<String> classifyHonor(BigDecimal gpa);

    List<HonorStudentDto> getHonorStudents(
        Integer semester, String schoolYear, BigDecimal minGrade, String honorLabel
    );
}
