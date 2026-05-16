package com.gradingsystem.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Full user profile returned by all user-management endpoints. Password is never included. */
@Getter
@Builder
public class UserResponseDto {

    private String id;
    private String username;
    private String email;
    private String fullName;
    private Integer roleId;
    private String roleName;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
}
