package com.gradingsystem.service.impl;

import com.gradingsystem.dto.role.PermissionDto;
import com.gradingsystem.dto.role.RoleDto;
import com.gradingsystem.entity.Permission;
import com.gradingsystem.entity.Role;
import com.gradingsystem.exception.ResourceNotFoundException;
import com.gradingsystem.mapper.RoleMapper;
import com.gradingsystem.repository.PermissionRepository;
import com.gradingsystem.repository.RoleRepository;
import com.gradingsystem.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository       roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper           roleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAllWithPermissions()
            .stream()
            .map(roleMapper::toDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDto getRoleById(Integer id) {
        return roleMapper.toDto(findRoleOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDto> getPermissionsByRoleId(Integer id) {
        return findRoleOrThrow(id).getPermissions()
            .stream()
            .map(roleMapper::toPermissionDto)
            .toList();
    }

    @Override
    @Transactional
    public RoleDto updateRolePermissions(Integer id, List<Integer> permissionIds) {
        Role role = findRoleOrThrow(id);

        // Validate every requested ID exists before touching the role
        List<Permission> found = permissionRepository.findAllById(permissionIds);
        if (found.size() != permissionIds.size()) {
            Set<Integer> foundIds = found.stream().map(Permission::getId).collect(Collectors.toSet());
            List<Integer> missing = permissionIds.stream()
                .filter(pid -> !foundIds.contains(pid))
                .toList();
            throw new ResourceNotFoundException("Permissions not found with IDs: " + missing);
        }

        // Full replacement of the permission set
        role.setPermissions(new HashSet<>(found));

        // @Transactional auto-flushes the dirty entity; explicit save for clarity
        return roleMapper.toDto(roleRepository.save(role));
    }

    // -----------------------------------------------------------------------

    private Role findRoleOrThrow(Integer id) {
        return roleRepository.findByIdWithPermissions(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }
}
