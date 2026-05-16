package com.gradingsystem.service.impl;

import com.gradingsystem.dto.subject.SubjectRequest;
import com.gradingsystem.dto.subject.SubjectResponse;
import com.gradingsystem.entity.Subject;
import com.gradingsystem.exception.DuplicateResourceException;
import com.gradingsystem.exception.ResourceNotFoundException;
import com.gradingsystem.mapper.SubjectMapper;
import com.gradingsystem.repository.SubjectRepository;
import com.gradingsystem.service.AuditLogService;
import com.gradingsystem.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper     subjectMapper;
    private final AuditLogService   auditLogService;

    @Override
    @Transactional
    public SubjectResponse createSubject(SubjectRequest request) {
        if (subjectRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Subject", "code", request.getCode());
        }
        Subject subject = subjectMapper.toEntity(request);
        subject.setActive(true);
        Subject saved = subjectRepository.save(subject);
        auditLogService.log("CREATE", "Subject", String.valueOf(saved.getId()));
        return subjectMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Integer id) {
        return subjectMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> listSubjects(String search) {
        String filter = blankToNull(search);
        return subjectRepository.searchActive(filter)
            .stream()
            .map(subjectMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public SubjectResponse updateSubject(Integer id, SubjectRequest request) {
        Subject subject = findOrThrow(id);
        if (!request.getCode().equals(subject.getCode())
                && subjectRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new DuplicateResourceException("Subject", "code", request.getCode());
        }
        subjectMapper.updateEntity(request, subject);
        Subject saved = subjectRepository.save(subject);
        auditLogService.log("UPDATE", "Subject", String.valueOf(id));
        return subjectMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SubjectResponse deactivateSubject(Integer id) {
        Subject subject = findOrThrow(id);
        subject.setActive(false);
        Subject saved = subjectRepository.save(subject);
        auditLogService.log("DEACTIVATE", "Subject", String.valueOf(id));
        return subjectMapper.toResponse(saved);
    }

    private Subject findOrThrow(Integer id) {
        return subjectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subject", id));
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
