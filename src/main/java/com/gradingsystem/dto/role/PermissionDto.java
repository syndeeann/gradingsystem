package com.gradingsystem.dto.role;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PermissionDto {

    private Integer id;
    private String name;
    private String resource;
    private String action;
    private String description;
}
