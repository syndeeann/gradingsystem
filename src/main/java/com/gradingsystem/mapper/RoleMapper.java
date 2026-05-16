package com.gradingsystem.mapper;

import com.gradingsystem.dto.role.PermissionDto;
import com.gradingsystem.dto.role.RoleDto;
import com.gradingsystem.entity.Permission;
import com.gradingsystem.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct will automatically apply toPermissionDto(Permission) when mapping
 * the Set&lt;Permission&gt; → List&lt;PermissionDto&gt; inside toDto(Role).
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "active", source = "active")
    RoleDto toDto(Role role);

    PermissionDto toPermissionDto(Permission permission);
}
