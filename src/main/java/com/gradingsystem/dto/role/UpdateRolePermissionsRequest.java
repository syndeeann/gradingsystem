package com.gradingsystem.dto.role;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateRolePermissionsRequest {

    /** Full replacement: any permission not in this list is removed from the role. */
    @NotNull(message = "permissionIds must not be null (send an empty list to clear all)")
    private List<Integer> permissionIds;
}
