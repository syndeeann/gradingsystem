package com.gradingsystem.service.impl;

import com.gradingsystem.dto.honor.HonorStudentDto;
import com.gradingsystem.dto.honor.HonorThresholdDto;
import com.gradingsystem.dto.honor.HonorThresholdRequest;
import com.gradingsystem.entity.HonorThreshold;
import com.gradingsystem.entity.User;
import com.gradingsystem.exception.DuplicateResourceException;
import com.gradingsystem.exception.ResourceNotFoundException;
import com.gradingsystem.repository.GradeRepository;
import com.gradingsystem.repository.HonorThresholdRepository;
import com.gradingsystem.repository.UserRepository;
import com.gradingsystem.service.AuditLogService;
import com.gradingsystem.service.HonorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HonorServiceImpl implements HonorService {

    private final HonorThresholdRepository honorThresholdRepository;
    private final GradeRepository          gradeRepository;
    private final UserRepository           userRepository;
    private final AuditLogService          auditLogService;

    // -----------------------------------------------------------------------
    // Threshold CRUD
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<HonorThresholdDto> getThresholds() {
        return honorThresholdRepository.findByActiveTrueOrderByMinGradeAsc()
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Override
    @Transactional
    public HonorThresholdDto createThreshold(HonorThresholdRequest request) {
        if (honorThresholdRepository.existsByLabel(request.getLabel())) {
            throw new DuplicateResourceException("HonorThreshold", "label", request.getLabel());
        }
        HonorThreshold threshold = HonorThreshold.builder()
            .label(request.getLabel())
            .minGrade(request.getMinGrade())
            .maxGrade(request.getMaxGrade())
            .active(true)
            .build();
        HonorThreshold saved = honorThresholdRepository.save(threshold);
        auditLogService.log("CREATE", "HonorThreshold", String.valueOf(saved.getId()));
        return toDto(saved);
    }

    @Override
    @Transactional
    public HonorThresholdDto updateThreshold(Integer id, HonorThresholdRequest request) {
        HonorThreshold threshold = findOrThrow(id);
        if (!request.getLabel().equals(threshold.getLabel())
                && honorThresholdRepository.existsByLabelAndIdNot(request.getLabel(), id)) {
            throw new DuplicateResourceException("HonorThreshold", "label", request.getLabel());
        }
        threshold.setLabel(request.getLabel());
        threshold.setMinGrade(request.getMinGrade());
        threshold.setMaxGrade(request.getMaxGrade());
        HonorThreshold saved = honorThresholdRepository.save(threshold);
        auditLogService.log("UPDATE", "HonorThreshold", String.valueOf(id));
        return toDto(saved);
    }

    @Override
    @Transactional
    public HonorThresholdDto deactivateThreshold(Integer id) {
        HonorThreshold threshold = findOrThrow(id);
        threshold.setActive(false);
        HonorThreshold saved = honorThresholdRepository.save(threshold);
        auditLogService.log("DEACTIVATE", "HonorThreshold", String.valueOf(id));
        return toDto(saved);
    }

    // -----------------------------------------------------------------------
    // GPA and honor logic
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public BigDecimal computeGpa(String studentId, Integer semester, String schoolYear) {
        return gradeRepository.computeTermGpa(studentId, semester, schoolYear)
            .map(g -> g.setScale(2, RoundingMode.HALF_UP))
            .orElse(BigDecimal.ZERO.setScale(2));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> classifyHonor(BigDecimal gpa) {
        if (gpa == null || gpa.compareTo(BigDecimal.ZERO) == 0) return Optional.empty();
        return honorThresholdRepository.findMatchingThreshold(gpa)
            .map(HonorThreshold::getLabel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HonorStudentDto> getHonorStudents(
            Integer semester, String schoolYear, BigDecimal minGrade, String honorLabel) {

        // Fetch all student GPAs for the term in one query
        List<Object[]> rawGpas = gradeRepository.computeAllTermGpas(semester, schoolYear);

        // Build studentId → gpa map
        Map<String, BigDecimal> gpaMap = rawGpas.stream()
            .filter(row -> row[1] != null)
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((BigDecimal) row[1]).setScale(2, RoundingMode.HALF_UP)
            ));

        // Fetch student entities in one query
        List<String> studentIds = new ArrayList<>(gpaMap.keySet());
        Map<String, User> studentMap = userRepository.findAllById(studentIds)
            .stream()
            .collect(Collectors.toMap(User::getId, u -> u));

        // Build result with honor classification and apply filters
        List<HonorStudentDto> result = gpaMap.entrySet().stream()
            .map(entry -> {
                String sid = entry.getKey();
                BigDecimal gpa = entry.getValue();
                String label = classifyHonor(gpa).orElse(null);
                User student = studentMap.get(sid);
                if (student == null) return null;
                return HonorStudentDto.builder()
                    .studentId(sid)
                    .studentName(student.getFullName())
                    .username(student.getUsername())
                    .gpa(gpa)
                    .honorLabel(label)
                    .build();
            })
            .filter(dto -> dto != null)
            .filter(dto -> dto.getHonorLabel() != null)
            .filter(dto -> minGrade == null || dto.getGpa().compareTo(minGrade) >= 0)
            .filter(dto -> honorLabel == null
                        || honorLabel.equalsIgnoreCase(dto.getHonorLabel()))
            .sorted((a, b) -> b.getGpa().compareTo(a.getGpa()))
            .toList();

        // Add sequential rank
        List<HonorStudentDto> ranked = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            ranked.add(result.get(i).toBuilder().rank(i + 1).build());
        }
        return ranked;
    }

    // -----------------------------------------------------------------------

    private HonorThreshold findOrThrow(Integer id) {
        return honorThresholdRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("HonorThreshold", id));
    }

    private HonorThresholdDto toDto(HonorThreshold h) {
        return HonorThresholdDto.builder()
            .id(h.getId())
            .label(h.getLabel())
            .minGrade(h.getMinGrade())
            .maxGrade(h.getMaxGrade())
            .active(h.isActive())
            .createdAt(h.getCreatedAt())
            .build();
    }
}
