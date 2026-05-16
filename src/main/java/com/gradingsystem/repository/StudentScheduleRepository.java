package com.gradingsystem.repository;

import com.gradingsystem.entity.StudentSchedule;
import com.gradingsystem.entity.StudentSchedule.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentScheduleRepository extends JpaRepository<StudentSchedule, Integer> {

    List<StudentSchedule> findByStudentId(String studentId);

    List<StudentSchedule> findByScheduleId(Integer scheduleId);

    List<StudentSchedule> findByStudentIdAndStatus(String studentId, EnrollmentStatus status);

    boolean existsByStudentIdAndScheduleId(String studentId, Integer scheduleId);

    boolean existsByStudentIdAndScheduleIdAndStatusNot(String studentId, Integer scheduleId, EnrollmentStatus status);

    Optional<StudentSchedule> findByStudentIdAndScheduleId(String studentId, Integer scheduleId);

    @Query("""
        SELECT ss FROM StudentSchedule ss
        JOIN FETCH ss.student
        JOIN FETCH ss.schedule sc
        JOIN FETCH sc.subject
        JOIN FETCH sc.teacher
        WHERE ss.id = :id
        """)
    Optional<StudentSchedule> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
        SELECT ss FROM StudentSchedule ss
        JOIN FETCH ss.student
        JOIN FETCH ss.schedule sc
        JOIN FETCH sc.subject
        JOIN FETCH sc.teacher
        WHERE ss.student.id = :studentId
        ORDER BY ss.enrollmentDate DESC
        """)
    List<StudentSchedule> findByStudentIdWithDetails(@Param("studentId") String studentId);

    @Query("""
        SELECT ss FROM StudentSchedule ss
        JOIN FETCH ss.student
        JOIN FETCH ss.schedule sc
        JOIN FETCH sc.subject
        WHERE sc.id = :scheduleId
        ORDER BY ss.student.fullName
        """)
    List<StudentSchedule> findByScheduleIdWithDetails(@Param("scheduleId") Integer scheduleId);
}
