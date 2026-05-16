package com.gradingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(nullable = false, length = 50)
    private String room;

    @Column(name = "day_of_week", nullable = false, length = 30)
    private String dayOfWeek;

    @Column(name = "time_start", nullable = false)
    private LocalTime timeStart;

    @Column(name = "time_end", nullable = false)
    private LocalTime timeEnd;

    // 1 = First Semester, 2 = Second Semester, 3 = Summer
    @Column(nullable = false)
    private Integer semester;

    @Column(name = "school_year", nullable = false, length = 9)
    private String schoolYear;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
