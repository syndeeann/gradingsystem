package com.gradingsystem.service.impl;

import com.gradingsystem.dto.common.PageResponse;
import com.gradingsystem.dto.user.ChangeRoleRequest;
import com.gradingsystem.dto.user.CreateUserRequest;
import com.gradingsystem.dto.user.UpdateUserRequest;
import com.gradingsystem.dto.user.UserResponseDto;
import com.gradingsystem.entity.Role;
import com.gradingsystem.entity.User;
import com.gradingsystem.exception.DuplicateResourceException;
import com.gradingsystem.exception.ResourceNotFoundException;
import com.gradingsystem.exception.UnauthorizedException;
import com.gradingsystem.mapper.UserMapper;
import com.gradingsystem.repository.RoleRepository;
import com.gradingsystem.repository.UserRepository;
import com.gradingsystem.service.UserService;
import com.gradingsystem.util.PageUtils;
import com.gradingsystem.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository  userRepository;
    private final RoleRepository  roleRepository;
    private final UserMapper      userMapper;
    private final PasswordEncoder passwordEncoder;

    // -----------------------------------------------------------------------
    // Create
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public UserResponseDto createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        Role role = resolveRole(request.getRoleId());

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(true);

        return userMapper.toDto(userRepository.save(user));
    }

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponseDto> listUsers(String roleName, Boolean active,
                                                   String search, Pageable pageable) {
        // Normalise blank strings to null so the JPQL null-checks behave correctly
        String roleFilter   = blankToNull(roleName);
        String searchFilter = blankToNull(search);

        return PageUtils.toPageResponse(
            userRepository.findWithFilters(roleFilter, active, searchFilter, pageable),
            userMapper::toDto
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(String id) {
        return userMapper.toDto(findUserOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsersByRole(String roleName) {
        return userRepository.findByRoleNameAndActiveTrue(roleName)
            .stream()
            .map(userMapper::toDto)
            .toList();
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public UserResponseDto updateUser(String id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);

        // Guard uniqueness for fields that are actually changing
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())
            && userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())
            && userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        userMapper.updateFromRequest(request, user);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto changeRole(String id, ChangeRoleRequest request) {
        User user = findUserOrThrow(id);
        user.setRole(resolveRole(request.getRoleId()));
        return userMapper.toDto(userRepository.save(user));
    }

    // -----------------------------------------------------------------------
    // Activate / Deactivate
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public UserResponseDto activateUser(String id) {
        User user = findUserOrThrow(id);
        user.setActive(true);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto deactivateUser(String id) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new UnauthorizedException("You cannot deactivate your own account");
        }

        User user = findUserOrThrow(id);
        user.setActive(false);
        return userMapper.toDto(userRepository.save(user));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private User findUserOrThrow(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private Role resolveRole(Integer roleId) {
        return roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
