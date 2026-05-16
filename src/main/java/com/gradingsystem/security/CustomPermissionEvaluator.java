package com.gradingsystem.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    /**
     * Checks whether the authenticated user holds an authority matching
     * the given permission string (e.g. "GRADES:READ").
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // TODO: add resource-level ownership checks (e.g. student can only view own grades)
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equalsIgnoreCase(permission.toString()));
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        // TODO: implement object-level security using targetId and targetType
        return hasPermission(authentication, null, permission);
    }
}
