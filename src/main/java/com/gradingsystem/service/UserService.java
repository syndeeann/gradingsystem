package com.gradingsystem.service;

import com.gradingsystem.dto.common.PageResponse;
import com.gradingsystem.dto.user.ChangeRoleRequest;
import com.gradingsystem.dto.user.CreateUserRequest;
import com.gradingsystem.dto.user.UpdateUserRequest;
import com.gradingsystem.dto.user.UserResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserResponseDto createUser(CreateUserRequest request);

    PageResponse<UserResponseDto> listUsers(String roleName, Boolean active, String search, Pageable pageable);

    UserResponseDto getUserById(String id);

    UserResponseDto updateUser(String id, UpdateUserRequest request);

    UserResponseDto changeRole(String id, ChangeRoleRequest request);

    UserResponseDto activateUser(String id);

    UserResponseDto deactivateUser(String id);

    List<UserResponseDto> getUsersByRole(String roleName);
}
