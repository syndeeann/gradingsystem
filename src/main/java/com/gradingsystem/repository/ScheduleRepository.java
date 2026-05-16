package com.gradingsystem.repository;

import com.gradingsystem.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    @Query("""
        SELECT s FROM Schedule s
        JOIN FETCH s.subject
        JOIN FETCH s.teacher
        WHERE s.id = :id
        """)
    Optional<Schedule> findByIdWithDetails(@Param("id") Integer id);

    @Query(value = """
        SELECT s FROM Schedule s
        JOIN FETCH s.subject
        JOIN FETCH s.teacher
        WHERE s.active = true
          AND (:teacherId  IS NULL OR s.teacher.id    = :teacherId)
          AND (:subjectId  IS NULL OR s.subject.id    = :subjectId)
          AND (:semester   IS NULL OR s.semester       = :semester)
          AND (:schoolYear IS NULL OR s.schoolYear     = :schoolYear)
        """,
        countQuery = """
        SELECT COUNT(s) FROM Schedule s
        WHERE s.active = true
          AND (:teacherId  IS NULL OR s.teacher.id    = :teacherId)
          AND (:subjectId  IS NULL OR s.subject.id    = :subjectId)
          AND (:semester   IS NULL OR s.semester       = :semester)
          AND (:schoolYear IS NULL OR s.schoolYear     = :schoolYear)
        """)
    Page<Schedule> findWithFilters(
        @Param("teacherId")  String teacherId,
        @Param("subjectId")  Integer subjectId,
        @Param("semester")   Integer semester,
        @Param("schoolYear") String schoolYear,
        Pageable pageable
    );

    @Query("""
        SELECT s FROM Schedule s
        WHERE s.teacher.id = :teacherId
          AND s.semester   = :semester
          AND s.schoolYear = :schoolYear
          AND s.active     = true
          AND s.id        <> :excludeId
        """)
    List<Schedule> findTeacherSchedulesForConflictCheck(
        @Param("teacherId")  String teacherId,
        @Param("semester")   Integer semester,
        @Param("schoolYear") String schoolYear,
        @Param("excludeId")  Integer excludeId
    );

    List<Schedule> findByTeacherIdAndActiveTrue(String teacherId);

    List<Schedule> findBySubjectIdAndActiveTrue(Integer subjectId);

    List<Schedule> findBySemesterAndSchoolYearAndActiveTrue(Integer semester, String schoolYear);
}
