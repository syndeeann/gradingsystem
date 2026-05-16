package com.gradingsystem.dto.auth;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LoginResponse {

    private String token;
    private String tokenType;   // always "Bearer"
    private long expiresIn;     // seconds
    private String userId;
    private String username;
    private String fullName;
    private String role;
    private List<String> permissions;
}
