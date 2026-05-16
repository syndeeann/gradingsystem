package com.gradingsystem.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private String id;
    private String username;
    private String email;
    private String fullName;
    private String roleName;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
