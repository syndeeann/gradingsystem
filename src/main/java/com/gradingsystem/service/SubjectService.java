package com.gradingsystem.service;

import com.gradingsystem.dto.subject.SubjectRequest;
import com.gradingsystem.dto.subject.SubjectResponse;

import java.util.List;

public interface SubjectService {

    SubjectResponse createSubject(SubjectRequest request);

    SubjectResponse getSubjectById(Integer id);

    List<SubjectResponse> listSubjects(String search);

    SubjectResponse updateSubject(Integer id, SubjectRequest request);

    SubjectResponse deactivateSubject(Integer id);
}
