package com.gradingsystem.repository;

import com.gradingsystem.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {

    Optional<Subject> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Integer id);

    List<Subject> findByActiveTrue();

    @Query("""
        SELECT s FROM Subject s
        WHERE s.active = true
          AND (:search IS NULL
               OR LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY s.code
        """)
    List<Subject> searchActive(@Param("search") String search);
}
