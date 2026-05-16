package com.gradingsystem.mapper;

import com.gradingsystem.dto.user.CreateUserRequest;
import com.gradingsystem.dto.user.UpdateUserRequest;
import com.gradingsystem.dto.user.UserRequest;
import com.gradingsystem.dto.user.UserResponse;
import com.gradingsystem.dto.user.UserResponseDto;
import com.gradingsystem.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // -----------------------------------------------------------------------
    // New API — used by UserController / UserService
    // -----------------------------------------------------------------------

    @Mapping(target = "roleId",   source = "role.id")
    @Mapping(target = "roleName", source = "role.name")
    UserResponseDto toDto(User user);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "role",         ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "lastLogin",    ignore = true)
    @Mapping(target = "active",       ignore = true)
    User toEntity(CreateUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "role",         ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "lastLogin",    ignore = true)
    @Mapping(target = "active",       ignore = true)
    void updateFromRequest(UpdateUserRequest request, @MappingTarget User user);

    // -----------------------------------------------------------------------
    // Legacy — kept so AuthServiceImpl and any older call sites still compile
    // -----------------------------------------------------------------------

    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "active",   source = "active")
    UserResponse toResponse(User user);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "role",         ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "lastLogin",    ignore = true)
    @Mapping(target = "active",       ignore = true)
    User toEntity(UserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "role",         ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "lastLogin",    ignore = true)
    @Mapping(target = "active",       ignore = true)
    void updateEntity(UserRequest request, @MappingTarget User user);
}
