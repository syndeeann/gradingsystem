package com.gradingsystem.repository;

import com.gradingsystem.entity.HonorThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface HonorThresholdRepository extends JpaRepository<HonorThreshold, Integer> {

    List<HonorThreshold> findByActiveTrueOrderByMinGradeAsc();

    boolean existsByLabel(String label);

    boolean existsByLabelAndIdNot(String label, Integer id);

    @Query("""
        SELECT h FROM HonorThreshold h
        WHERE h.active = true
          AND :grade >= h.minGrade
          AND :grade <= h.maxGrade
        ORDER BY h.minGrade DESC
        """)
    List<HonorThreshold> findMatchingThresholds(@Param("grade") BigDecimal grade);

    default Optional<HonorThreshold> findMatchingThreshold(BigDecimal grade) {
        List<HonorThreshold> matches = findMatchingThresholds(grade);
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
    }
}
