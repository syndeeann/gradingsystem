package com.gradingsystem.repository;

import com.gradingsystem.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Integer> {

    Optional<Grade> findByStudentScheduleId(Integer studentScheduleId);

    @Query("""
        SELECT g FROM Grade g
        JOIN FETCH g.studentSchedule ss
        JOIN FETCH ss.student
        JOIN FETCH ss.schedule sc
        JOIN FETCH sc.subject
        JOIN FETCH sc.teacher
        JOIN FETCH g.recordedBy
        WHERE ss.student.id = :studentId
        ORDER BY sc.schoolYear DESC, sc.semester DESC
        """)
    List<Grade> findByStudentIdWithDetails(@Param("studentId") String studentId);

    @Query("""
        SELECT g FROM Grade g
        JOIN FETCH g.studentSchedule ss
        JOIN FETCH ss.student
        JOIN FETCH ss.schedule sc
        JOIN FETCH sc.subject
        JOIN FETCH g.recordedBy
        WHERE sc.id = :scheduleId
        ORDER BY ss.student.fullName
        """)
    List<Grade> findByScheduleIdWithDetails(@Param("scheduleId") Integer scheduleId);

    @Query("""
        SELECT SUM(g.gradeValue * sc.subject.units) / NULLIF(SUM(sc.subject.units), 0)
        FROM Grade g
        JOIN g.studentSchedule ss
        JOIN ss.schedule sc
        WHERE ss.student.id = :studentId
        """)
    Optional<BigDecimal> computeWeightedGpa(@Param("studentId") String studentId);

    @Query("""
        SELECT SUM(g.gradeValue * sc.subject.units) / NULLIF(SUM(sc.subject.units), 0)
        FROM Grade g
        JOIN g.studentSchedule ss
        JOIN ss.schedule sc
        WHERE ss.student.id = :studentId
          AND sc.semester   = :semester
          AND sc.schoolYear = :schoolYear
        """)
    Optional<BigDecimal> computeTermGpa(
        @Param("studentId")  String studentId,
        @Param("semester")   Integer semester,
        @Param("schoolYear") String schoolYear
    );

    @Query("""
        SELECT ss.student.id, SUM(g.gradeValue * sc.subject.units) / NULLIF(SUM(sc.subject.units), 0)
        FROM Grade g
        JOIN g.studentSchedule ss
        JOIN ss.schedule sc
        WHERE sc.semester   = :semester
          AND sc.schoolYear = :schoolYear
        GROUP BY ss.student.id
        """)
    List<Object[]> computeAllTermGpas(
        @Param("semester")   Integer semester,
        @Param("schoolYear") String schoolYear
    );
}
