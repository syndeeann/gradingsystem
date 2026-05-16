package com.gradingsystem.dto.schedule;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class ScheduleResponse {

    private Integer id;
    private Integer subjectId;
    private String subjectCode;
    private String subjectName;
    private String teacherId;
    private String teacherName;
    private String room;
    private String dayOfWeek;
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private Integer semester;
    private String schoolYear;
    private boolean active;
}
