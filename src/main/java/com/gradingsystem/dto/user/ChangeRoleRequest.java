package com.gradingsystem.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeRoleRequest {

    @NotNull(message = "Role ID is required")
    private Integer roleId;
}
