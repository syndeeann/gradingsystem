package com.gradingsystem.dto.subject;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubjectResponse {

    private Integer id;
    private String code;
    private String name;
    private String description;
    private Integer units;
    private boolean active;
}
