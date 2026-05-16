package com.gradingsystem.controller;

import com.gradingsystem.dto.role.PermissionDto;
import com.gradingsystem.dto.role.RoleDto;
import com.gradingsystem.dto.role.UpdateRolePermissionsRequest;
import com.gradingsystem.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role and permission management. "
    + "Requires ROLES_READ / ROLES_UPDATE authorities (add to schema if not seeded).")
public class RoleController {

    private final RoleService roleService;

    // -----------------------------------------------------------------------
    // GET /api/roles  — all roles with embedded permissions
    // -----------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAuthority('ROLES_READ')")
    @Operation(summary = "List all roles with their permission sets")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Roles returned"),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    // -----------------------------------------------------------------------
    // GET /api/roles/{id}/permissions
    // -----------------------------------------------------------------------

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLES_READ')")
    @Operation(summary = "Get the permissions assigned to a specific role")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Permissions returned"),
        @ApiResponse(responseCode = "404", description = "Role not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<PermissionDto>> getPermissions(
        @Parameter(description = "Role ID") @PathVariable Integer id
    ) {
        return ResponseEntity.ok(roleService.getPermissionsByRoleId(id));
    }

    // -----------------------------------------------------------------------
    // PUT /api/roles/{id}/permissions  — full replacement
    // -----------------------------------------------------------------------

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLES_UPDATE')")
    @Operation(
        summary = "Replace the permission set for a role",
        description = "Full replacement: any permission not in permissionIds is removed. "
                    + "Send an empty list to revoke all permissions from the role. "
                    + "Returns 404 if any permissionId does not exist."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Permissions updated, updated role returned"),
        @ApiResponse(responseCode = "404", description = "Role or one or more permissions not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<RoleDto> updatePermissions(
        @Parameter(description = "Role ID") @PathVariable Integer id,
        @Valid @RequestBody UpdateRolePermissionsRequest request
    ) {
        return ResponseEntity.ok(roleService.updateRolePermissions(id, request.getPermissionIds()));
    }
}
