package com.gradingsystem.dto.role;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RoleDto {

    private Integer id;
    private String name;
    private String description;
    private boolean active;
    private List<PermissionDto> permissions;
}
