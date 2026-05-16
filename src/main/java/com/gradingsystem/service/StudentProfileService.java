package com.gradingsystem.service;

import com.gradingsystem.dto.student.StudentProfileResponse;

public interface StudentProfileService {

    StudentProfileResponse getProfile(String studentId);
}
