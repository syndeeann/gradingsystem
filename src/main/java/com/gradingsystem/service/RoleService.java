package com.gradingsystem.service;

import com.gradingsystem.dto.role.PermissionDto;
import com.gradingsystem.dto.role.RoleDto;

import java.util.List;

public interface RoleService {

    List<RoleDto> getAllRoles();

    RoleDto getRoleById(Integer id);

    List<PermissionDto> getPermissionsByRoleId(Integer id);

    RoleDto updateRolePermissions(Integer id, List<Integer> permissionIds);
}
