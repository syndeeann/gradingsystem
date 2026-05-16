package com.gradingsystem.security;

import com.gradingsystem.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spring Security principal. Two factory methods:
 *  - create(User)  — used by CustomUserDetailsService at login time (DB available)
 *  - fromToken(…)  — used by JwtAuthFilter on every request (no DB)
 *
 * Authority format: "ROLE_<ROLE>" + "RESOURCE_ACTION" (e.g. "GRADES_CREATE").
 * See SECURITY.md for the full naming convention.
 */
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final String id;
    private final String username;
    private final String email;       // null when created from token
    private final String password;    // null when created from token
    private final Collection<? extends GrantedAuthority> authorities;

    // -----------------------------------------------------------------------
    // Factory: from JPA entity (used at login / UserDetailsService)
    // -----------------------------------------------------------------------

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Stream.concat(
            Stream.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())),
            user.getRole().getPermissions().stream()
                .map(p -> new SimpleGrantedAuthority(p.getResource() + "_" + p.getAction()))
        ).collect(Collectors.toList());

        return new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPasswordHash(),
            authorities
        );
    }

    // -----------------------------------------------------------------------
    // Factory: from JWT claims (used by JwtAuthFilter, no DB call)
    // -----------------------------------------------------------------------

    public static UserPrincipal fromToken(String userId,
                                          String username,
                                          List<? extends GrantedAuthority> authorities) {
        return new UserPrincipal(userId, username, null, null, authorities);
    }

    // -----------------------------------------------------------------------
    // UserDetails contract
    // -----------------------------------------------------------------------

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
