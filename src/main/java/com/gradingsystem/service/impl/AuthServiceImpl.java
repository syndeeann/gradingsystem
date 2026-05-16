package com.gradingsystem.service.impl;

import com.gradingsystem.config.JwtConfig;
import com.gradingsystem.dto.auth.LoginRequest;
import com.gradingsystem.dto.auth.LoginResponse;
import com.gradingsystem.entity.User;
import com.gradingsystem.exception.ResourceNotFoundException;
import com.gradingsystem.repository.UserRepository;
import com.gradingsystem.security.JwtUtil;
import com.gradingsystem.security.UserPrincipal;
import com.gradingsystem.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil               jwtUtil;
    private final UserRepository        userRepository;
    private final JwtConfig             jwtConfig;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Throws BadCredentialsException on failure — caught by GlobalExceptionHandler
        // TODO: add per-IP / per-username rate limiting before calling authenticate()
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // Permissions list: all non-ROLE_ authorities already in RESOURCE_ACTION format
        List<String> permissions = principal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> !a.startsWith("ROLE_"))
            .toList();

        // Derive role name by stripping the "ROLE_" prefix
        String role = principal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_"))
            .findFirst()
            .map(a -> a.substring(5))
            .orElse("");

        String token = jwtUtil.generateToken(principal, principal.getId(), role, permissions);

        // Stamp last_login; also retrieves fullName which is not stored in the principal
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", principal.getId()));
        user.setLastLogin(LocalDateTime.now());
        // save() is a no-op because @Transactional auto-flushes the dirty entity

        return LoginResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(jwtConfig.getExpiration() / 1000L)   // convert ms → seconds
            .userId(principal.getId())
            .username(principal.getUsername())
            .fullName(user.getFullName())
            .role(role)
            .permissions(permissions)
            .build();
    }

    @Override
    public void logout(String token) {
        // TODO: push token jti to a Redis blacklist so it is rejected before expiry
    }
}
