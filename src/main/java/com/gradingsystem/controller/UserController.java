package com.gradingsystem.controller;

import com.gradingsystem.dto.common.PageResponse;
import com.gradingsystem.dto.user.ChangeRoleRequest;
import com.gradingsystem.dto.user.CreateUserRequest;
import com.gradingsystem.dto.user.UpdateUserRequest;
import com.gradingsystem.dto.user.UserResponseDto;
import com.gradingsystem.service.UserService;
import com.gradingsystem.util.PageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User account management")
public class UserController {

    private final UserService userService;

    // -----------------------------------------------------------------------
    // POST /api/users  — create user (Admin only)
    // -----------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAuthority('USERS_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user account")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    // -----------------------------------------------------------------------
    // GET /api/users  — list with optional filters
    // -----------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAuthority('USERS_READ')")
    @Operation(summary = "List users with optional filters and pagination")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User list returned"),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PageResponse<UserResponseDto>> listUsers(
        @Parameter(description = "Filter by role name, e.g. TEACHER")
        @RequestParam(required = false) String role,

        @Parameter(description = "Filter by active status")
        @RequestParam(required = false) Boolean isActive,

        @Parameter(description = "Search by name, email, or username (case-insensitive)")
        @RequestParam(required = false) String search,

        @Parameter(description = "Page index (0-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size,

        @Parameter(description = "Sort field", example = "createdAt")
        @RequestParam(defaultValue = "createdAt") String sortBy,

        @Parameter(description = "Sort direction: asc or desc", example = "desc")
        @RequestParam(defaultValue = "desc") String direction
    ) {
        Pageable pageable = PageUtils.toPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(userService.listUsers(role, isActive, search, pageable));
    }

    // -----------------------------------------------------------------------
    // GET /api/users/{id}  — single profile
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USERS_READ')")
    @Operation(summary = "Get a user by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserResponseDto> getUserById(
        @Parameter(description = "User UUID") @PathVariable String id
    ) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // -----------------------------------------------------------------------
    // PUT /api/users/{id}  — update profile fields
    // -----------------------------------------------------------------------

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USERS_UPDATE')")
    @Operation(
        summary = "Update user info",
        description = "Partial update: omit (null) any field you do not want to change. "
                    + "Duplicate username/email returns 409."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User updated"),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Username or email already taken",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserResponseDto> updateUser(
        @Parameter(description = "User UUID") @PathVariable String id,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // -----------------------------------------------------------------------
    // PATCH /api/users/{id}/deactivate
    // -----------------------------------------------------------------------

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('USERS_UPDATE')")
    @Operation(
        summary = "Deactivate a user account",
        description = "Sets is_active = false. Returns 403 if you try to deactivate your own account."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User deactivated"),
        @ApiResponse(responseCode = "403", description = "Cannot deactivate own account",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserResponseDto> deactivateUser(
        @Parameter(description = "User UUID") @PathVariable String id
    ) {
        return ResponseEntity.ok(userService.deactivateUser(id));
    }

    // -----------------------------------------------------------------------
    // PATCH /api/users/{id}/activate
    // -----------------------------------------------------------------------

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('USERS_UPDATE')")
    @Operation(summary = "Re-activate a previously deactivated user account")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User activated"),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserResponseDto> activateUser(
        @Parameter(description = "User UUID") @PathVariable String id
    ) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    // -----------------------------------------------------------------------
    // PUT /api/users/{id}/role  — reassign role (Admin only)
    // -----------------------------------------------------------------------

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Change the role assigned to a user",
        description = "Admin-only. Changing a user's role takes effect on their next login "
                    + "because the current JWT still carries the old permissions."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Role updated"),
        @ApiResponse(responseCode = "404", description = "User or role not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserResponseDto> changeRole(
        @Parameter(description = "User UUID") @PathVariable String id,
        @Valid @RequestBody ChangeRoleRequest request
    ) {
        return ResponseEntity.ok(userService.changeRole(id, request));
    }

    // -----------------------------------------------------------------------
    // GET /api/users/by-role/{roleName}  — convenience filter (kept for compat)
    // -----------------------------------------------------------------------

    @GetMapping("/by-role/{roleName}")
    @PreAuthorize("hasAuthority('USERS_READ')")
    @Operation(summary = "List active users by role name")
    public ResponseEntity<List<UserResponseDto>> getUsersByRole(
        @Parameter(description = "Role name, e.g. STUDENT") @PathVariable String roleName
    ) {
        return ResponseEntity.ok(userService.getUsersByRole(roleName));
    }
}
