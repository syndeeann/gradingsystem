package com.gradingsystem.util;

import com.gradingsystem.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static String getCurrentUserId() {
        return getCurrentUser()
            .map(UserPrincipal::getId)
            .orElseThrow(() -> new IllegalStateException("No authenticated user in context"));
    }

    public static String getCurrentUsername() {
        return getCurrentUser()
            .map(UserPrincipal::getUsername)
            .orElseThrow(() -> new IllegalStateException("No authenticated user in context"));
    }

    public static boolean hasAuthority(String authority) {
        // TODO: case-insensitive comparison if needed
        return getCurrentUser()
            .map(u -> u.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority)))
            .orElse(false);
    }
}
